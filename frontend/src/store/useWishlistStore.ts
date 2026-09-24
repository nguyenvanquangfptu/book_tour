import { create } from 'zustand';
import { WishlistService } from '../services/WishlistService';

/**
 * Danh sách yêu thích được lưu Ở SERVER (bảng wishlists), không còn nằm trong
 * localStorage của trình duyệt như trước.
 *
 * Lý do đổi:
 *  - Trước đây wishlist gắn với TRÌNH DUYỆT chứ không gắn với TÀI KHOẢN: đăng
 *    nhập tài khoản khác trên cùng máy vẫn thấy wishlist cũ, còn đăng nhập cùng
 *    tài khoản trên máy khác thì rỗng.
 *  - Store cũ lưu bản sao title/price/imageUrl tại thời điểm bấm tim, nên tour
 *    đổi giá thì hiện giá cũ và tour bị xóa thì thẻ vẫn còn nhưng bấm vào là
 *    hỏng. Giờ dữ liệu luôn đọc tươi từ bảng tours.
 *
 * Vì vậy store này CỐ TÌNH không dùng middleware `persist`: nguồn sự thật duy
 * nhất là server, giữ thêm một bản trong localStorage chỉ tạo ra hai phiên bản
 * lệch nhau.
 */
export interface WishlistItem {
  id: number;
  slug?: string;
  title: string;
  price: number;
  imageUrl: string;
  destination: string;
  duration: string;
}

interface WishlistState {
  wishlist: WishlistItem[];
  isLoading: boolean;
  fetchWishlist: () => Promise<void>;
  addToWishlist: (tourId: number) => Promise<void>;
  removeFromWishlist: (tourId: number) => Promise<void>;
  toggleWishlist: (item: WishlistItem) => Promise<void>;
  isInWishlist: (tourId: number) => boolean;
  clearWishlist: () => void;
}

/** Backend trả về TourResponse - lấy đúng các trường giao diện cần. */
const toWishlistItems = (tours: any[]): WishlistItem[] =>
  (tours || []).map((t) => ({
    id: t.id,
    slug: t.slug,
    title: t.title,
    price: t.price,
    imageUrl: t.imageUrl,
    destination: t.destination,
    duration: t.duration,
  }));

export const useWishlistStore = create<WishlistState>((set, get) => ({
  wishlist: [],
  isLoading: false,

  fetchWishlist: async () => {
    set({ isLoading: true });
    try {
      const data = await WishlistService.getMyWishlist();
      set({ wishlist: toWishlistItems(data) });
    } catch (error) {
      console.error('Không lấy được danh sách yêu thích', error);
      // Chưa đăng nhập hoặc phiên hết hạn -> để danh sách rỗng
      set({ wishlist: [] });
    } finally {
      set({ isLoading: false });
    }
  },

  addToWishlist: async (tourId) => {
    set({ isLoading: true });
    try {
      const data = await WishlistService.addToWishlist(tourId);
      set({ wishlist: toWishlistItems(data) });
    } catch (error) {
      console.error('Không thêm được vào danh sách yêu thích', error);
      throw error;
    } finally {
      set({ isLoading: false });
    }
  },

  removeFromWishlist: async (tourId) => {
    set({ isLoading: true });
    try {
      const data = await WishlistService.removeFromWishlist(tourId);
      set({ wishlist: toWishlistItems(data) });
    } catch (error) {
      console.error('Không bỏ được khỏi danh sách yêu thích', error);
      throw error;
    } finally {
      set({ isLoading: false });
    }
  },

  toggleWishlist: async (item) => {
    if (get().isInWishlist(item.id)) {
      await get().removeFromWishlist(item.id);
    } else {
      await get().addToWishlist(item.id);
    }
  },

  isInWishlist: (tourId) => get().wishlist.some((item) => item.id === tourId),

  /** Dùng khi đăng xuất - chỉ xóa state trên máy, không đụng dữ liệu server. */
  clearWishlist: () => set({ wishlist: [] }),
}));
