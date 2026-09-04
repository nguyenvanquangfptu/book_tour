import api from '../api/axiosConfig';

/**
 * Mỗi endpoint đều trả về danh sách wishlist đầy đủ SAU thao tác, giống cách
 * CartService làm. Nhờ vậy client không phải tự đoán trạng thái mới rồi phải
 * gọi thêm một lượt để đồng bộ lại.
 */
export const WishlistService = {
  getMyWishlist: async () => {
    const response = await api.get('/wishlist');
    return response.data;
  },

  addToWishlist: async (tourId: number) => {
    const response = await api.post(`/wishlist/${tourId}`);
    return response.data;
  },

  removeFromWishlist: async (tourId: number) => {
    const response = await api.delete(`/wishlist/${tourId}`);
    return response.data;
  }
};
