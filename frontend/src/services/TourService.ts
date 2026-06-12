import api from '../api/axiosConfig';

export const TourService = {
  getTours: async (page = 0, size = 12, keyword = '', destination = '', startDate = '', endDate = '', maxPrice?: number, sortBy = 'id', sortDir = 'ASC', tourTypes: string[] = [], transports: string[] = []) => {
    let url = `/tours/search?page=${page}&size=${size}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    if (destination) url += `&destination=${encodeURIComponent(destination)}`;
    if (startDate) url += `&startDate=${encodeURIComponent(startDate)}`;
    if (endDate) url += `&endDate=${encodeURIComponent(endDate)}`;
    if (maxPrice && maxPrice > 0) url += `&maxPrice=${maxPrice}`;
    if (sortBy) url += `&sortBy=${sortBy}`;
    if (sortDir) url += `&sortDir=${sortDir}`;
    if (tourTypes.length > 0) url += `&tourTypes=${tourTypes.join(',')}`;
    if (transports.length > 0) url += `&transports=${transports.join(',')}`;
    const response = await api.get(url);
    return response.data?.data || response.data;
  },

  getTourById: async (id: string | number) => {
    const response = await api.get(`/tours/${id}`);
    return response.data?.data || response.data;
  },

  getTourReviews: async (tourId: string | number) => {
    const response = await api.get(`/reviews/tour/${tourId}`);
    return response.data?.data || response.data;
  },

  createTour: async (tourData: any) => {
    const response = await api.post('/tours', tourData);
    return response.data;
  },

  updateTour: async (id: string | number, tourData: any) => {
    const response = await api.put(`/tours/${id}`, tourData);
    return response.data;
  },

  deleteTour: async (id: string | number) => {
    const response = await api.delete(`/tours/${id}`);
    return response.data;
  }
};
