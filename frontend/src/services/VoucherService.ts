import api from '../api/axiosConfig';

export const VoucherService = {
  getAllVouchers: async () => {
    try {
      const response: any = await api.get('/vouchers');
      return response.data || response;
    } catch (error) {
      console.error('Error fetching vouchers:', error);
      throw error;
    }
  },

  getVoucherById: async (id: string | number) => {
    const response: any = await api.get(`/vouchers/${id}`);
    return response.data || response;
  },

  getVoucherByCode: async (code: string) => {
    try {
      const response: any = await api.get(`/vouchers/code/${code}`);
      return response.data || response;
    } catch (error) {
      console.error('Error fetching voucher by code:', error);
      throw error;
    }
  },

  createVoucher: async (voucherData: any) => {
    const response: any = await api.post('/vouchers', voucherData);
    return response.data || response;
  },

  updateVoucher: async (id: string | number, voucherData: any) => {
    const response: any = await api.put(`/vouchers/${id}`, voucherData);
    return response.data || response;
  },

  deleteVoucher: async (id: string | number) => {
    const response: any = await api.delete(`/vouchers/${id}`);
    return response.data || response;
  }
};
