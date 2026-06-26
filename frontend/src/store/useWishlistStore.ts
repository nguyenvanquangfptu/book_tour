import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface WishlistItem {
  id: number;
  title: string;
  price: number;
  imageUrl: string;
  destination: string;
  duration: string;
}

interface WishlistState {
  wishlist: WishlistItem[];
  addToWishlist: (item: WishlistItem) => void;
  removeFromWishlist: (tourId: number) => void;
  toggleWishlist: (item: WishlistItem) => void;
  isInWishlist: (tourId: number) => boolean;
  clearWishlist: () => void;
}

export const useWishlistStore = create<WishlistState>()(
  persist(
    (set, get) => ({
      wishlist: [],
      
      addToWishlist: (newItem) => {
        set((state) => {
          if (!state.wishlist.find((item) => item.id === newItem.id)) {
            return { wishlist: [...state.wishlist, newItem] };
          }
          return state;
        });
      },

      removeFromWishlist: (tourId) => {
        set((state) => ({
          wishlist: state.wishlist.filter((item) => item.id !== tourId)
        }));
      },

      toggleWishlist: (item) => {
        const state = get();
        if (state.isInWishlist(item.id)) {
          state.removeFromWishlist(item.id);
        } else {
          state.addToWishlist(item);
        }
      },

      isInWishlist: (tourId) => {
        return get().wishlist.some((item) => item.id === tourId);
      },

      clearWishlist: () => set({ wishlist: [] }),
    }),
    {
      name: 'booking-wishlist-storage',
    }
  )
);
