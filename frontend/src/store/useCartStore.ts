import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface CartItem {
  tourId: number;
  tourTitle: string;
  price: number;
  imageUrl: string;
  guests: number;
  startDate: string;
}

interface CartState {
  cart: CartItem[];
  addToCart: (item: CartItem) => void;
  removeFromCart: (tourId: number, startDate: string) => void;
  clearCart: () => void;
  getCartCount: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      cart: [],
      
      addToCart: (newItem) => {
        set((state) => {
          const existingItemIndex = state.cart.findIndex(
            (item) => item.tourId === newItem.tourId && item.startDate === newItem.startDate
          );

          if (existingItemIndex >= 0) {
            const updatedCart = [...state.cart];
            updatedCart[existingItemIndex].guests += newItem.guests;
            return { cart: updatedCart };
          }

          return { cart: [...state.cart, newItem] };
        });
      },

      removeFromCart: (tourId, startDate) => {
        set((state) => ({
          cart: state.cart.filter((item) => !(item.tourId === tourId && item.startDate === startDate))
        }));
      },

      clearCart: () => set({ cart: [] }),

      getCartCount: () => get().cart.length,
    }),
    {
      name: 'booking-cart-storage',
      // Optional: customize the storage key per user
      // if you want guest and logged-in user to have different carts.
      // But keeping it simple for now as it's the standard behavior.
    }
  )
);
