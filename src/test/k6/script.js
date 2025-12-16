import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
//  thresholds: {
//    http_req_failed: ['rate<0.01'],
//    http_req_duration: ['p95<500'],
//  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/member/login`,
    JSON.stringify({ id: 'test1', password: 'qwer1234' }),
    { headers: { 'Content-Type': 'application/json' } }
  );


  check(loginRes, { 'login 200': (r) => r.status === 200 });
  const body = JSON.parse(loginRes.body);
  // 서버 응답 형태에 맞게 키 이름 수정하세요 (예: accessToken, token, jwt 등)
  const token = body.data.accessToken;
  console.log('::::', token)

  if (!token) throw new Error(`No token in login response: ${loginRes.body.data.accessToken}`);

  return { token };
}

export default function (data) {
  const headers = {
//    Authorization: `Bearer ${data.token}`,
    'Content-Type': 'application/json',
    'x-access-token': data.token
  };

  const res = http.get(`${BASE_URL}/member/myinfo`, { headers });
  check(res, { 'me 200': (r) => r.status === 200 });

  console.log(res.body)

  sleep(1);
}
