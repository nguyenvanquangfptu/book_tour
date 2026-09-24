import api from '../api/axiosConfig';

export const AccommodationService = {
  getAll: async () => {
    const response: any = await api.get('/accommodations');
    return response?.data || response;
  },

  getById: async (id: string | number) => {
    const response: any = await api.get(`/accommodations/${id}`);
    return response?.data || response;
  },

  create: async (data: any) => {
    const response = await api.post('/accommodations', data);
    return response;
  },

  update: async (id: string | number, data: any) => {
    const response = await api.put(`/accommodations/${id}`, data);
    return response;
  },

  delete: async (id: string | number) => {
    const response = await api.delete(`/accommodations/${id}`);
    return response;
  }
};
