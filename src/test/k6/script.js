import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 토큰(=계정) 개수는 VU 이상 권장
const TOKEN_COUNT = Number(__ENV.TOKEN_COUNT || 300);
const USER_PREFIX = __ENV.USER_PREFIX || 'testuser';
const PASSWORD = __ENV.PASSWORD || 'qwer1234';

// 동시성 테스트 모드: contended | distributed | mixed
const MODE = __ENV.MODE || 'contended';

// 경쟁 모드에서 모든 유저가 노릴 ticketId
const HOT_TICKET_ID = Number(__ENV.HOT_TICKET_ID || 1);

// 분산 모드에서 사용할 ticketId 범위 (예: 1~1000)
const TICKET_ID_START = Number(__ENV.TICKET_ID_START || 1);
const TICKET_ID_END = Number(__ENV.TICKET_ID_END || 200);

// 요청 간 텀(실서비스 느낌을 위해 약간 랜덤도 가능)
const SLEEP_SEC = Number(__ENV.SLEEP_SEC || 0.1);

// 예매 성공/실패를 status 코드로 구분하는 경우가 많아서 체크를 넓게 잡음
// (서버가 200/201/204 등을 쓸 수도 있고, 실패는 409/400/429/500 등이 나올 수 있음)
function isSuccessStatus(s) {
  return s === 200 || s === 201 || s === 204;
}

export const options = {
  scenarios: {
    reserve: {
      executor: 'constant-arrival-rate',
      rate: 3000,
      timeUnit: '1s',
      preAllocatedVUs: 3000,
      maxVUs: 6000,
      duration : '1m'
    },
  },
  thresholds: {
    // http_req_failed: ['rate<0.05'],        // 동시성 테스트는 실패(409 등)가 정상일 수 있어 실패율 기준 완화
    // http_req_duration: ['p(95)<800'],      // 락 경합 시 느려질 수 있어 기준 완화(원하면 조정)
  },
  // setupTimeout: '10m'
};

export function setup() {
  const tokens = [];

  for (let i = 1; i <= TOKEN_COUNT; i++) {
    const id = `${USER_PREFIX}${i}`;

    const res = http.post(
      `${BASE_URL}/member/login`,
      JSON.stringify({ id, password: PASSWORD }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    if (res.status !== 200) {
      console.error(`[LOGIN FAIL] id=${id} status=${res.status} body=${res.body}`);
      throw new Error(`Login failed for ${id}`);
    }

    const parsed = res.json();
    const token = parsed?.data?.accessToken;
    if (!token) {
      console.error(`[TOKEN MISSING] id=${id} parsed=${JSON.stringify(parsed)}`);
      throw new Error(`No token for ${id}`);
    }

    tokens.push(token);
  }

  return { tokens };
}

// VU별 토큰 고정 매핑
function pickToken(data) {
  const idx = (__VU - 1) % data.tokens.length;
  return data.tokens[idx];
}

// ticketId 선택 로직 (모드별)
function pickTicketId() {
  if (MODE === 'contended') {
    // 모두 같은 티켓을 동시에 노림 (락/중복/재고 감소 검증용)
    return HOT_TICKET_ID;
  }

  if (MODE === 'distributed') {
    // 각 VU가 서로 다른 티켓을 주로 노림 (처리량 측정)
    const span = Math.max(1, TICKET_ID_END - TICKET_ID_START + 1);
    return TICKET_ID_START + ((__VU - 1) % span);
  }

  // mixed: 70%는 HOT(경쟁), 30%는 분산(현실 혼합)
  // (확률은 원하는 대로 바꿔도 됨)
  const r = Math.random();
  if (r < 0.7) return HOT_TICKET_ID;

  const span = Math.max(1, TICKET_ID_END - TICKET_ID_START + 1);
  return TICKET_ID_START + (Math.floor(Math.random() * span));
}

export default function (data) {
  const token = pickToken(data);
  const ticketId = pickTicketId();

  const headers = {
    'x-access-token': token,
  };

  const getTicketInfo = http.get(`http://localhost:8080/ticket/detail/1`);

  if(getTicketInfo.status == 200){
    const url = `${BASE_URL}/ticket/reserve/${ticketId}`;
    const res = http.post(url, null, { headers });
  
    // ✅ 동시성 테스트에서는 "실패"도 정상 결과일 수 있어요.
    // 예: 매진이면 409/400 등
    // 그래서 체크는 두 단계로 나눠서 관측하기 좋게 함.
    check(res, {
      'reserve responded': (r) => r.status !== 0, // connection reset 등 네트워크 실패만 잡기
    });
  
    // 성공/경합 실패를 구분해서 로그/지표로 보고 싶으면 아래처럼
    if (!isSuccessStatus(res.status) && res.status !== 409 && res.status !== 400) {
      // 409(중복/매진), 400(비즈니스 실패) 등은 케이스에 따라 정상일 수 있음
      // 진짜 이상한 에러만 찍기
      // console.error(`[RESERVE ERROR] status=${res.status} body=${res.body}`);
    }
  
    sleep(SLEEP_SEC);
  }

  
}
