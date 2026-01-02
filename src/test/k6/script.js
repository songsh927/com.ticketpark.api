import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";


// ===== Metrics =====
const listLatency = new Trend("list_latency");
const detailLatency = new Trend("detail_latency");

const waitingEnterLatency = new Trend("waiting_enter_latency"); // POST waiting
const waitingPollLatency = new Trend("waiting_poll_latency");   // GET waiting
const reserveLatency = new Trend("reserve_latency");            // POST reserve

const bizReserveSuccess = new Rate("biz_reserve_success");      // 2xx
const bizReserveRejected = new Rate("biz_reserve_rejected");    // 400/409/410 등
const bizReserveThrottled = new Rate("biz_reserve_throttled");  // 429/503 등
const queueAdmittedRate = new Rate("queue_admitted");           // waiting GET에서 302 받았는지
const queueTimeoutRate = new Rate("queue_timeout");             // 폴링 타임아웃으로 포기했는지

const waitingEnterAttempts = new Counter("waiting_enter_attempts");
const waitingPollAttempts = new Counter("waiting_poll_attempts");
const reserveAttempts = new Counter("reserve_attempts");

// ===== Config =====
function cfg() {
  return {
    baseUrl: __ENV.BASE_URL || "http://54.180.30.56/api",
    // baseUrl: __ENV.BASE_URL || "http://localhost:8080",
    ticketId: __ENV.TICKET_ID || "1",

    listPath: "/ticket/list",
    detailPath: "/ticket/detail",
    reservePath: "/ticket/reserve",
    waitingPath: "/ticket/waiting",

    // Simulation knobs
    warmUsers: Number(__ENV.WARM_USERS || 50),
    openUsers: Number(__ENV.OPEN_USERS || 300),
    openSeconds: Number(__ENV.OPEN_SECONDS || 20),
    tailSeconds: Number(__ENV.TAIL_SECONDS || 120),

    // Queue polling behavior
    pollIntervalSec: Number(__ENV.POLL_INTERVAL_SEC || 3), // ✅ 2초
    maxPollSeconds: Number(__ENV.MAX_POLL_SECONDS || 240), // ✅ 최대 대기(기본 120초)
  };
}

function jitterSleepMs(ms) {
  const jitter = 0.85 + Math.random() * 0.3; // 약간만 흔들림
  sleep((ms * jitter) / 1000);
}

// ===== Token selection (VU 고정) =====
function memberHeaders() {
    const memberIdx = __VU;                  // 1 ~ VU
    const memberId = `testuser${memberIdx}`; // testuser1 ~ testuser100000
  
    return {
      memberIdx: String(memberIdx),
      memberId: memberId,
    };
  }

// ===== Requests =====
function requestPublic(method, url, body, tagName, timeout = "5s") {
  const params = {
    headers: { "Content-Type": "application/json" },
    tags: { name: tagName },
    timeout,
    redirects: 0, // 안전하게 기본값; public에서도 상관없음
  };
  if (method === "GET") return http.get(url, params);
  if (method === "POST") return http.post(url, body || "{}", params);
  throw new Error(`Unsupported method: ${method}`);
}

function requestAuthed(method, url, body, tagName, timeout = "5s", redirects = 0) {
  const c = cfg();
  const params = {
    headers: {
      "Content-Type": "application/json",
      ...memberHeaders(),
    },
    tags: { name: tagName },
    timeout,
    redirects, // ✅ 302를 우리가 직접 감지해야 하므로 기본 0
  };
  if (method === "GET") return http.get(url, params);
  if (method === "POST") return http.post(url, body || "{}", params);
  throw new Error(`Unsupported method: ${method}`);
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

function enterQueue(ticketId) {
  const c = cfg();
  const url = `${c.baseUrl}${c.waitingPath}/${ticketId}`;

  const res = requestAuthed("POST", url, JSON.stringify({}), "waiting_enter", "5s", 0);
  waitingEnterAttempts.add(1);
  waitingEnterLatency.add(res.timings.duration);

  // 보통 2xx면 대기열 진입 성공. 409/429 등 정책이면 여기서 분기 가능
  check(res, { "waiting enter <500": (r) => r.status < 500 });

  console.log(res)

  return res;
}

function pollQueue(ticketId) {
  const c = cfg();
  const url = `${c.baseUrl}${c.waitingPath}/${ticketId}`;

  // ✅ redirects=0 으로 302를 그대로 받기
  const res = requestAuthed("GET", url, null, "waiting_poll", "5s", 0);
  waitingPollAttempts.add(1);
  waitingPollLatency.add(res.timings.duration);

  // 302가 아니어도 서버가 살아있는지만 체크
  check(res, { "waiting poll <500": (r) => r.status < 500 });

  return res;
}

function classifyReserve(status) {
  if (status >= 200 && status < 300) return "SUCCESS";
  if (status === 409 || status === 410 || status === 400) return "REJECTED";
  if (status === 429 || status === 503) return "THROTTLED";
  return "ERROR";
}

function doReserve(ticketId) {
  const c = cfg();
  const url = `${c.baseUrl}${c.reservePath}/${ticketId}`;

  const res = requestAuthed("POST", url, JSON.stringify({}), "ticket_reserve", "5s", 0);
  reserveAttempts.add(1);
  reserveLatency.add(res.timings.duration);

  const kind = classifyReserve(res.status);
  bizReserveSuccess.add(kind === "SUCCESS");
  bizReserveRejected.add(kind === "REJECTED");
  bizReserveThrottled.add(kind === "THROTTLED");

  check(res, { "reserve not 5xx (except 503)": (r) => r.status < 500 || r.status === 503 });

  return { res, kind };
}

// ===== Queue flow: enter -> poll every 2s -> on 302 reserve =====
function queueThenReserve(ticketId) {
  const c = cfg();

  // 1) 대기열 진입
  const enterRes = enterQueue(ticketId);

  // 대기열 진입이 실패(예: 401, 403)인 경우는 바로 종료
  if (enterRes.status >= 500) return;

  // 2) 폴링
  const deadline = Date.now() + c.maxPollSeconds * 1000;

  while (Date.now() < deadline) {
    const res = pollQueue(ticketId);

    if (res.status === 302) {
      queueAdmittedRate.add(true);

      // 3) 302면 reserve 시도
      doReserve(ticketId);
      return;
    }

    // 302가 아니면 2초 대기 후 다시 폴링
    sleep(c.pollIntervalSec);
  }

  // 제한 시간 내 302 못 받음 (대기열 장기체류)
  queueAdmittedRate.add(false);
  queueTimeoutRate.add(true);
}

// ===== Scenarios =====
export const options = {
  scenarios: {
    // 오픈 전: list/detail 새로고침 (public)
    // warmup_browsing: {
    //   executor: "constant-vus",
    //   vus: Number(__ENV.WARM_USERS || 50),
    //   duration: "2m",
    //   exec: "browse_preopen",
    //   startTime: "0s",
    // },

    // 오픈 순간: 대기열 진입 + 폴링 + 302시 reserve
    open_spike_queue: {
      executor: "constant-vus",
      vus: Number(__ENV.OPEN_USERS || 10000),
      duration: `${Number(__ENV.OPEN_SECONDS || 60)}s`,
      exec: "open_queue_flow",
      startTime: "0s",
    },

    // 오픈 후 꼬리: detail 폴링 + (일부) 다시 대기열 시도
    post_open_tail: {
      executor: "constant-vus",
      vus: 150,
      duration: `${Number(__ENV.TAIL_SECONDS || 120)}s`,
      exec: "post_open_behavior",
      startTime: `1m`,
    },
  },

  thresholds: {
    waiting_enter_latency: ["p(95)<1200"],
    waiting_poll_latency: ["p(95)<1200"],
    reserve_latency: ["p(95)<1500"],

    // 참고: http_req_failed는 302/4xx를 실패로 잡을 수 있어 의미가 애매할 수 있음
    // 안정성만 보고 싶으면 완화하거나 제거 추천
    http_req_failed: ["rate<0.35"],
  },

  summaryTrendStats: ["min", "med", "p(90)", "p(95)", "max"],
};

// ===== Scenario functions =====
export function browse_preopen() {
  const c = cfg();

  callList();
  sleep(0.5 + Math.random() * 1.2);

  const loops = 3 + Math.floor(Math.random() * 6);
  for (let i = 0; i < loops; i++) {
    callDetail(c.ticketId);
    jitterSleepMs(500 + Math.random() * 1000);
  }
}

export function open_queue_flow() {
  const c = cfg();
  // 오픈 순간: 대기열 → 302면 예약
  queueThenReserve(c.ticketId);

  // 유저가 버튼 누르고 화면 보는 시간 느낌
  sleep(0.2 + Math.random() * 0.6);
}

export function post_open_behavior() {
  const c = cfg();
  const end = Date.now() + c.tailSeconds * 1000;

  while (Date.now() < end) {
    callDetail(c.ticketId);

    // 10%는 다시 대기열 진입 시도(취소표/새로고침 습관)
    if (Math.random() < 0.10) {
      queueThenReserve(c.ticketId);
    }

    sleep(0.8 + Math.random() * 2.2);
  }
}
