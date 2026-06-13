import api from '../api/axiosConfig';

export const VoucherService = {
  getAllVouchers: async () => {
    try {
      const response = await api.get('/vouchers');
      return response.data.data;
    } catch (error) {
      console.error('Error fetching vouchers:', error);
      throw error;
    }
  },

  getVoucherById: async (id: string | number) => {
    const response = await api.get(`/vouchers/${id}`);
    return response.data;
  },

  getVoucherByCode: async (code: string) => {
    try {
      const response = await api.get(`/vouchers/code/${code}`);
      return response.data.data;
    } catch (error) {
      console.error('Error fetching voucher by code:', error);
      throw error;
    }
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
