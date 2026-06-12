import api from '../api/axiosConfig';

export const VoucherService = {
  getVouchers: async () => {
    const response = await api.get('/vouchers');
    return response.data;
  },

  getVoucherById: async (id: string | number) => {
    const response = await api.get(`/vouchers/${id}`);
    return response.data;
  },

  getVoucherByCode: async (code: string) => {
    const response = await api.get(`/vouchers/code/${code}`);
    return response.data;
  },

  createVoucher: async (voucherData: any) => {
    const response = await api.post('/vouchers', voucherData);
    return response.data;
  },

  updateVoucher: async (id: string | number, voucherData: any) => {
    const response = await api.put(`/vouchers/${id}`, voucherData);
    return response.data;
  },

  deleteVoucher: async (id: string | number) => {
    const response = await api.delete(`/vouchers/${id}`);
    return response.data;
  }
};
