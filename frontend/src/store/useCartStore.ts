import { create } from 'zustand';
import api from '../api/axiosConfig';

export interface CartItem {
  id?: number;
  tourId: number;
  tourTitle: string;
  price: number;
  imageUrl: string;
  guests: number;
  startDate: string;
}

interface CartState {
  cart: CartItem[];
  isLoading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (item: CartItem) => Promise<void>;
  removeFromCart: (itemId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  getCartCount: () => number;
}

export const useCartStore = create<CartState>((set, get) => ({
  cart: [],
  isLoading: false,
  
  fetchCart: async () => {
    set({ isLoading: true });
    try {
      const response = await api.get('/cart');
      if (response.data && response.data.data) {
        set({ cart: response.data.data.items || [] });
      }
    } catch (error) {
      console.error('Failed to fetch cart', error);
      // If unauthorized, clear cart
      set({ cart: [] });
    } finally {
      set({ isLoading: false });
    }
  },

  addToCart: async (newItem) => {
    set({ isLoading: true });
    try {
      const response = await api.post('/cart/add', {
        tourId: newItem.tourId,
        guests: newItem.guests,
        startDate: newItem.startDate
      });
      if (response.data && response.data.data) {
        set({ cart: response.data.data.items || [] });
      }
    } catch (error) {
      console.error('Failed to add to cart', error);
      throw error;
    } finally {
      set({ isLoading: false });
    }
  },

  removeFromCart: async (itemId) => {
    set({ isLoading: true });
    try {
      const response = await api.delete(`/cart/remove/${itemId}`);
      if (response.data && response.data.data) {
        set({ cart: response.data.data.items || [] });
      }
    } catch (error) {
      console.error('Failed to remove from cart', error);
      throw error;
    } finally {
      set({ isLoading: false });
    }
  },

  clearCart: async () => {
    set({ isLoading: true });
    try {
      await api.delete('/cart/clear');
      set({ cart: [] });
    } catch (error) {
      console.error('Failed to clear cart', error);
      throw error;
    } finally {
      set({ isLoading: false });
    }
  },

  getCartCount: () => get().cart.length,
}));
