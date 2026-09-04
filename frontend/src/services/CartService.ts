import api from '../api/axiosConfig';

export interface CartItemRequest {
  tourId: number;
  guests: number;
  startDate: string;
}

export const CartService = {
  getMyCart: async () => {
    const response = await api.get('/cart');
    return response.data;
  },

  addToCart: async (request: CartItemRequest) => {
    const response = await api.post('/cart/add', request);
    return response.data;
  },

  removeFromCart: async (cartItemId: number) => {
    const response = await api.delete(`/cart/remove/${cartItemId}`);
    return response.data;
  },

  clearCart: async () => {
    const response = await api.delete('/cart/clear');
    return response.data;
  }
};
