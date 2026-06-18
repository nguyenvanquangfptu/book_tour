import api from '../api/axiosConfig';

export interface ReviewResponse {
  id: number;
  userId: number;
  username: string;
  tourId: number;
  rating: number;
  comment: string;
  createdAt: string;
}

export const ReviewService = {
  getRecentReviews: async (): Promise<ReviewResponse[]> => {
    try {
      const response = await api.get('/reviews/recent');
      return response.data || response;
    } catch (error) {
      console.error('Error fetching recent reviews:', error);
      return [];
    }
  },

  getReviewsByTour: async (tourId: number): Promise<ReviewResponse[]> => {
    try {
      const response = await api.get(`/reviews/tour/${tourId}`);
      return response.data || response;
    } catch (error) {
      console.error('Error fetching tour reviews:', error);
      return [];
    }
  }
};
