import api from '../api/axiosConfig';

export const UtilityService = {
  getAll: async () => {
    const response: any = await api.get('/utilities');
    return response?.data || response;
  },

  getById: async (id: string | number) => {
    const response: any = await api.get(`/utilities/${id}`);
    return response?.data || response;
  },

  create: async (data: any) => {
    const response = await api.post('/utilities', data);
    return response;
  },

  update: async (id: string | number, data: any) => {
    const response = await api.put(`/utilities/${id}`, data);
    return response;
  },

  delete: async (id: string | number) => {
    const response = await api.delete(`/utilities/${id}`);
    return response;
  }
};
