import api from '../api/axiosConfig';

export const UserService = {
  getMyProfile: async () => {
    const response = await api.get('/users/profile');
    return response.data?.data || response.data;
  },

  updateProfile: async (data: any) => {
    const response = await api.put('/users/profile', data);
    return response.data?.data || response.data;
  },

  changePassword: async (data: any) => {
    const response = await api.put('/users/profile/change-password', data);
    return response.data?.data || response.data;
  }
};
