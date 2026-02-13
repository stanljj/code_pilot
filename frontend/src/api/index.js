import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' },
});

// 演示用：Basic 认证（生产改为从企微回调拿到 token 后放在 Authorization: Bearer xxx）
function getAuth() {
  const token = sessionStorage.getItem('codepilot_token');
  if (token) return { Authorization: `Bearer ${token}` };
  const user = sessionStorage.getItem('codepilot_user');
  const pass = sessionStorage.getItem('codepilot_pass');
  if (user && pass) return { Authorization: 'Basic ' + btoa(`${user}:${pass}`) };
  return {};
}

api.interceptors.request.use((config) => {
  Object.assign(config.headers, getAuth());
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      sessionStorage.removeItem('codepilot_token');
      sessionStorage.removeItem('codepilot_user');
      sessionStorage.removeItem('codepilot_pass');
    }
    return Promise.reject(err);
  }
);

let lastResponse = null;
export function getLastResponse() {
  return lastResponse;
}
export function setLastResponse(res) {
  lastResponse = res;
}

export async function ask(content, sessionId, source = 'paste') {
  const { data } = await api.post('/v1/ask', { content, sessionId, source });
  lastResponse = data;
  return data;
}

export async function wecomLoginUrl() {
  const { data } = await api.get('/auth/wecom/login');
  return data.loginUrl;
}

export function setDemoAuth(username, password) {
  sessionStorage.setItem('codepilot_user', username);
  sessionStorage.setItem('codepilot_pass', password);
}

export default api;
