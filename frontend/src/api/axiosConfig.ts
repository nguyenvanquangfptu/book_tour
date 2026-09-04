import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL as string,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const originalRequest = error.config;
    const isAuthRequest = originalRequest.url?.includes('/auth/login') || 
                          originalRequest.url?.includes('/auth/google') || 
                          originalRequest.url?.includes('/auth/register');

    // 401 = phiên đăng nhập không hợp lệ/hết hạn -> đăng xuất.
    // 403 = đã đăng nhập nhưng không đủ quyền -> không đăng xuất, để component tự xử lý thông báo.
    if (error.response && error.response.status === 401 && !isAuthRequest) {
      const token = localStorage.getItem('token');
      if (token) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
