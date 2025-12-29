import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

// ===== Metrics =====
const loginLatency = new Trend("login_latency");
const listLatency = new Trend("list_latency");
const detailLatency = new Trend("detail_latency");
const reserveLatency = new Trend("reserve_latency");

const bizReserveSuccess = new Rate("biz_reserve_success");      // 2xx
const bizReserveRejected = new Rate("biz_reserve_rejected");    // 400/409/410 (매진/경합/중복 등)
const bizReserveThrottled = new Rate("biz_reserve_throttled");  // 429/503 (대기실/레이트리밋 등)
const authRefreshRate = new Rate("auth_refreshed");             // 401/403 → 재로그인 여부

const reserveAttempts = new Counter("reserve_attempts");

// ===== Config =====
function cfg() {
  return {
    baseUrl: __ENV.BASE_URL || "http://54.180.30.56",
    ticketId: __ENV.TICKET_ID || "1",

    // API paths
    loginPath: "/member/login",
    listPath: "/ticket/list",
    detailPath: "/ticket/detail",
    reservePath: "/ticket/reserve",

    // users.json
    usersFile: "./tmp/users.json",

    // 로그인 바디 필드명(고정)
    loginIdField: "id",
    loginPassField: "password",

    // 토큰 헤더 이름(고정)
    tokenHeaderName: "x-access-token",

    // Simulation knobs
    warmUsers: Number(__ENV.WARM_USERS || 50),
    openUsers: Number(__ENV.OPEN_USERS || 300),
    openSeconds: Number(__ENV.OPEN_SECONDS || 20),
    tailSeconds: Number(__ENV.TAIL_SECONDS || 120),

    // Reserve retry policy
    maxReserveRetries: Number(__ENV.MAX_RESERVE_RETRIES || 6),
    backoffMinMs: Number(__ENV.BACKOFF_MIN_MS || 80),
    backoffMaxMs: Number(__ENV.BACKOFF_MAX_MS || 700),

    // Pre-open detail refresh cadence
    preDetailMinMs: Number(__ENV.PRE_DETAIL_MIN_MS || 500),
    preDetailMaxMs: Number(__ENV.PRE_DETAIL_MAX_MS || 1500),
  };
}

function jitterSleepMs(ms) {
  const jitter = 0.7 + Math.random() * 0.6;
  sleep((ms * jitter) / 1000);
}

// ===== Users pool =====
// users.json: { "users":[{"id":"testuser1","password":"qwer1234"}, ...] }
function loadUsers() {
  const c = cfg();
  if (!c.usersFile) throw new Error('USERS_FILE 필요. 예) -e USERS_FILE=users.json');
  const raw = open(c.usersFile);
  const parsed = JSON.parse(raw);
  if (!parsed.users || !Array.isArray(parsed.users) || parsed.users.length === 0) {
    throw new Error("users.json에 users 배열이 없거나 비어있어.");
  }
  return parsed.users;
}
const USERS = loadUsers();

function userForVU() {
  return USERS[(__VU - 1) % USERS.length];
}

// ===== Auth per VU (고정 토큰) =====
let VU_TOKEN = ""; // VU별로 유지됨. x-access-token 값으로 사용

function loginOncePerVU() {
  // 이미 토큰 있으면 재사용 (VU 고정)
  if (VU_TOKEN) return VU_TOKEN;

  const c = cfg();
  const u = userForVU();

  const body = JSON.stringify({
    [c.loginIdField]: u.id,
    [c.loginPassField]: u.password,
  });

  const res = http.post(`${c.baseUrl}${c.loginPath}`, body, {
    headers: { "Content-Type": "application/json" },
    tags: { name: "member_login" },
    timeout: "5s",
  });

  loginLatency.add(res.timings.duration);

  const ok = check(res, { "login 2xx": (r) => r.status >= 200 && r.status < 300 });
  if (!ok) {
    throw new Error(
      `로그인 실패: status=${res.status}, body=${String(res.body).slice(0, 200)}`
    );
  }

  let accessToken;
  try {
    const j = res.json();
    accessToken = j?.data?.accessToken; // 고정 경로
  } catch (e) {
    accessToken = undefined;
  }

  if (!accessToken || typeof accessToken !== "string") {
    throw new Error(
      `로그인 응답에서 data.accessToken을 못 찾음. body=${String(res.body).slice(0, 200)}`
    );
  }

  // ✅ VU에 고정 저장
  VU_TOKEN = accessToken;
  return VU_TOKEN;
}

// 401/403이면 토큰 갱신(재로그인) 후 1회 재시도
function refreshLogin() {
  const c = cfg();
  const u = userForVU();

  const body = JSON.stringify({
    [c.loginIdField]: u.id,
    [c.loginPassField]: u.password,
  });

  const res = http.post(`${c.baseUrl}${c.loginPath}`, body, {
    headers: { "Content-Type": "application/json" },
    tags: { name: "member_login_refresh" },
    timeout: "5s",
  });

  loginLatency.add(res.timings.duration);

  const ok = check(res, { "refresh login 2xx": (r) => r.status >= 200 && r.status < 300 });
  if (!ok) return false;

  let accessToken;
  try {
    const j = res.json();
    accessToken = j?.data?.accessToken;
  } catch (_) {
    accessToken = undefined;
  }
  if (!accessToken || typeof accessToken !== "string") return false;

  VU_TOKEN = accessToken; // ✅ VU 토큰 갱신
  return true;
}

// ===== Requests =====

// 공개 API: 로그인 불필요 (헤더 없음)
function requestPublic(method, url, body, tagName, timeout = "5s") {
  const params = {
    headers: { "Content-Type": "application/json" },
    tags: { name: tagName },
    timeout,
  };

  if (method === "GET") return http.get(url, params);
  if (method === "POST") return http.post(url, body || "{}", params);
  throw new Error(`Unsupported method: ${method}`);
}

// 보호 API: reserve만 해당 (x-access-token 필요)
function requestReserve(method, url, body, tagName, timeout = "5s") {
  const c = cfg();
  const token = loginOncePerVU();

  const params = {
    headers: {
      "Content-Type": "application/json",
      [c.tokenHeaderName]: token, // ✅ x-access-token
    },
    tags: { name: tagName },
    timeout,
  };

  let res;
  if (method === "POST") res = http.post(url, body || "{}", params);
  else if (method === "GET") res = http.get(url, params);
  else throw new Error(`Unsupported method: ${method}`);

  // 토큰 만료/권한 문제면 재로그인 1회 후 재시도
  if (res.status === 401 || res.status === 403) {
    const refreshed = refreshLogin();
    authRefreshRate.add(refreshed);

    if (refreshed) {
      const params2 = {
        headers: {
          "Content-Type": "application/json",
          [c.tokenHeaderName]: VU_TOKEN,
        },
        tags: { name: `${tagName}_retry_after_refresh` },
        timeout,
      };
      if (method === "POST") res = http.post(url, body || "{}", params2);
      else res = http.get(url, params2);
    }
  } else {
    authRefreshRate.add(false);
  }

  return res;
}

// ===== Business calls =====
function callList() {
  const c = cfg();
  const res = requestPublic("GET", `${c.baseUrl}${c.listPath}`, null, "ticket_list", "5s");
  listLatency.add(res.timings.duration);
  check(res, { "list <500": (r) => r.status < 500 });
  return res;
}

function callDetail(ticketId) {
  const c = cfg();
  const res = requestPublic(
    "GET",
    `${c.baseUrl}${c.detailPath}/${ticketId}`,
    null,
    "ticket_detail",
    "5s"
  );
  detailLatency.add(res.timings.duration);
  check(res, { "detail <500": (r) => r.status < 500 });
  return res;
}

function classifyReserve(status) {
  if (status >= 200 && status < 300) return "SUCCESS";
  if (status === 409 || status === 410 || status === 400) return "REJECTED";
  if (status === 429 || status === 503) return "THROTTLED";
  return "ERROR";
}

function callReserve(ticketId) {
  const c = cfg();
  const res = requestReserve(
    "POST",
    `${c.baseUrl}${c.reservePath}/${ticketId}`,
    JSON.stringify({}),
    "ticket_reserve",
    "5s"
  );

  reserveAttempts.add(1);
  reserveLatency.add(res.timings.duration);

  const kind = classifyReserve(res.status);
  bizReserveSuccess.add(kind === "SUCCESS");
  bizReserveRejected.add(kind === "REJECTED");
  bizReserveThrottled.add(kind === "THROTTLED");

  check(res, {
    "reserve not 5xx (except 503)": (r) => r.status < 500 || r.status === 503,
  });

  return { res, kind };
}

// ===== Scenarios =====
export const options = {
  scenarios: {
    // 오픈 전: list/detail 새로고침 (로그인 불필요)
    warmup_browsing: {
      executor: "constant-vus",
      vus: Number(__ENV.WARM_USERS || 50),
      duration: "2m",
      exec: "browse_preopen",
      startTime: "0s",
    },

    // 오픈 순간: reserve 스파이크 (로그인 필요, VU 토큰 고정)
    open_spike: {
      executor: "constant-vus",
      vus: Number(__ENV.OPEN_USERS || 300),
      duration: `${Number(__ENV.OPEN_SECONDS || 20)}s`,
      exec: "reserve_open",
      startTime: "2m",
    },

    // 오픈 후: detail 폴링 + 간헐 reserve 재시도
    post_open_tail: {
      executor: "constant-vus",
      vus: Math.max(10, Math.floor(Number(__ENV.OPEN_USERS || 300) * 0.35)),
      duration: `${Number(__ENV.TAIL_SECONDS || 120)}s`,
      exec: "post_open_behavior",
      startTime: `2m${Number(__ENV.OPEN_SECONDS || 20)}s`,
    },
  },

  thresholds: {
    login_latency: ["p(95)<1500"],
    list_latency: ["p(95)<1200"],
    detail_latency: ["p(95)<1200"],
    reserve_latency: ["p(95)<1500"],

    http_req_failed: ["rate<0.35"], // 4xx도 실패로 잡힘(매진이면 올라갈 수 있음)
  },

  summaryTrendStats: ["min", "med", "p(90)", "p(95)", "max"],
};

// ===== Scenario functions =====
export function browse_preopen() {
  const c = cfg();

  // 오픈 전 행동: list 한 번 + detail 3~8회 새로고침 (로그인 불필요)
  callList();
  sleep(0.5 + Math.random() * 1.2);

  const loops = 3 + Math.floor(Math.random() * 6);
  for (let i = 0; i < loops; i++) {
    callDetail(c.ticketId);
    jitterSleepMs(c.preDetailMinMs + Math.random() * (c.preDetailMaxMs - c.preDetailMinMs));
  }
}

export function reserve_open() {
  const c = cfg();

  // 오픈 순간 일부는 “방금 들어와서” 로그인 처음 하는 느낌을 내고 싶으면:
  // (지금은 VU_TOKEN이 없으면 자동 loginOncePerVU가 수행됨)

  // 오픈 순간 1회 클릭
  let { kind } = callReserve(c.ticketId);
  if (kind === "SUCCESS") {
    sleep(1 + Math.random() * 2);
    return;
  }

  // 실패자 재시도 (연타형/간격형)
  const aggressive = Math.random() < 0.4;
  for (let i = 0; i < cfg().maxReserveRetries; i++) {
    if (kind === "REJECTED" && Math.random() < 0.8) return; // 매진이면 대부분 포기

    const base = aggressive ? c.backoffMinMs : c.backoffMaxMs;
    const step = Math.min(c.backoffMaxMs, base * (1 + i * 0.6));
    jitterSleepMs(step);

    ({ kind } = callReserve(c.ticketId));
    if (kind === "SUCCESS") {
      sleep(1 + Math.random() * 2);
      return;
    }
  }
}

export function post_open_behavior() {
  const c = cfg();

  const end = Date.now() + c.tailSeconds * 1000;
  while (Date.now() < end) {
    // 상세는 로그인 필요 없음
    callDetail(c.ticketId);

    // 10%는 취소표 기대하며 reserve 재시도 (reserve만 로그인/토큰 필요)
    if (Math.random() < 0.10) {
      callReserve(c.ticketId);
    }

    sleep(0.8 + Math.random() * 2.2);
  }
}

/*

k6 run \
  -e USERS_FILE="users.json" \
  -e TICKET_ID="1" \
  -e WARM_USERS=50 \
  -e OPEN_USERS=200 \
  -e OPEN_SECONDS=20 \
  -e TAIL_SECONDS=120 \
  ticketing-login-real.js

*/