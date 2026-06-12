import api from '../api/axiosConfig';

export const BookingService = {
  createBooking: async (bookingData: any) => {
    const response = await api.post('/bookings', bookingData);
    return response.data;
  },

  createVNPayUrl: async (bookingId: number) => {
    const response = await api.get(`/payments/create-vnpay-url?bookingId=${bookingId}`);
    return response.data;
  },

  getMyBookings: async () => {
    const response = await api.get('/bookings/my-bookings');
    return response.data?.data || response.data;
  },

  cancelBooking: async (id: number) => {
    const response = await api.put(`/bookings/${id}/cancel`);
    return response.data?.data || response.data;
  }
};
