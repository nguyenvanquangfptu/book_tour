import axios from 'axios';
import type { AxiosError, InternalAxiosRequestConfig } from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL as string;

/**
 * withCredentials: bắt buộc để trình duyệt gửi kèm cookie refresh_token.
 * Cookie đó là httpOnly nên đoạn code này không đọc được nó — và đó chính là
 * mục đích: một lỗ hổng XSS chỉ lấy được access token 15 phút trong
 * localStorage, không lấy được chìa khoá 30 ngày.
 */
const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Client riêng để gọi /auth/refresh — cố tình KHÔNG gắn interceptor.
 * Nếu dùng chung `api`, một lần refresh thất bại sẽ lại rơi vào interceptor 401
 * và gọi refresh tiếp, thành vòng lặp vô hạn.
 */
const refreshClient = axios.create({ baseURL, withCredentials: true });

/** Những endpoint mà 401 nghĩa là "sai thông tin", không phải "token hết hạn". */
const NO_REFRESH_PATHS = [
  '/auth/login',
  '/auth/register',
  '/auth/google',
  '/auth/refresh',
  '/auth/logout',
];

type RetriableConfig = InternalAxiosRequestConfig & { _retried?: boolean };

/**
 * Một lần refresh tại một thời điểm (single-flight).
 *
 * ĐÂY LÀ PHẦN BẮT BUỘC, KHÔNG PHẢI TỐI ƯU. Refresh token chỉ dùng được một
 * lần: nếu 5 request cùng hết hạn và mỗi request tự gọi /auth/refresh với cùng
 * một token, thì 4 lần sau sẽ bị server coi là DÙNG LẠI — nó thu hồi toàn bộ
 * phiên và gửi email cảnh báo cho người dùng. Ứng dụng tự tố cáo chính mình.
 *
 * Biến này giữ đúng một promise: request đầu tiên khởi động việc làm mới, các
 * request còn lại chờ chung kết quả rồi thử lại.
 */
let refreshInFlight: Promise<string> | null = null;

const refreshAccessToken = (): Promise<string> => {
  if (!refreshInFlight) {
    refreshInFlight = refreshClient
      .post('/auth/refresh')
      .then((response) => {
        const payload = response.data?.data;
        const token: string | undefined = payload?.token;
        if (!token) {
          throw new Error('Phản hồi refresh không có access token');
        }
        localStorage.setItem('token', token);
        // Backend trả kèm hồ sơ người dùng mới nhất — đồng bộ luôn để tên/avatar
        // đổi ở nơi khác cũng được cập nhật.
        localStorage.setItem('user', JSON.stringify(payload));
        return token;
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
};

/** Phiên không cứu được nữa: xoá sạch phía client và bắt đăng nhập lại. */
const hardLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
};

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response.data,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined;
    const status = error.response?.status;
    const url = original?.url ?? '';
    const skipRefresh = NO_REFRESH_PATHS.some((path) => url.includes(path));

    // 401 = access token hết hạn hoặc không hợp lệ -> thử làm mới đúng MỘT lần.
    // 403 = đã đăng nhập nhưng không đủ quyền -> không đăng xuất, để component
    //       tự hiển thị thông báo.
    if (status === 401 && original && !original._retried && !skipRefresh) {
      original._retried = true;
      try {
        const token = await refreshAccessToken();
        original.headers.Authorization = `Bearer ${token}`;
        return await api.request(original);
      } catch {
        // Refresh token cũng đã hết hạn, bị thu hồi, hoặc bị phát hiện dùng lại.
        hardLogout();
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
