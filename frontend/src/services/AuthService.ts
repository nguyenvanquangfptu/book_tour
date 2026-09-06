import api from '../api/axiosConfig';

export const AuthService = {
  login: async (credentials: any) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: any) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  googleLogin: async (idToken: string) => {
    const response = await api.post('/auth/google', { idToken });
    return response.data;
  },

  // Không còn logout() ở đây. Xoá localStorage là chưa đủ: cookie refresh_token
  // vẫn sống 30 ngày phía server, ai lấy được nó vẫn xin được access token mới.
  // Đăng xuất phải đi qua useAuthStore.logout() để gọi POST /auth/logout và thu
  // hồi cả family refresh token.

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    if (userStr) return JSON.parse(userStr);
    return null;
  },

  forgotPassword: async (email: string) => {
    const response = await api.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
    return response.data;
  },

  resetPassword: async (token: string, newPassword: string) => {
    const response = await api.post('/auth/reset-password', { token, newPassword });
    return response.data;
  }
};
