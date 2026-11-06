import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      sessionStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authApi = {
  register: data => api.post('/auth/register', data),
  login:    data => api.post('/auth/login', data),
};

export const fileApi = {
  // Do NOT set Content-Type manually — browser sets it automatically with correct boundary for multipart
  // Setting it manually drops the Authorization header from the interceptor
  upload:       form   => api.post('/files/upload', form),
  download:     id     => api.get('/files/download/' + id),
  share:        (id, p) => api.post('/files/share/' + id, null, { params: p }),
  delete:       id     => api.delete('/files/' + id),
  myFiles:      ()     => api.get('/files/my-files'),
  sharedWithMe: ()     => api.get('/files/shared-with-me'),
};

export const auditApi = {
  myLogs:      () => api.get('/audit/my-logs'),
  allLogs:     () => api.get('/audit/all'),
  verifyChain: () => api.get('/audit/verify-chain'),
  honeyAlerts: () => api.get('/audit/honey-alerts'),
};

export default api;
