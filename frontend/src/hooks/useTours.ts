import { useQuery } from '@tanstack/react-query';
import api from '../api/axiosConfig';

export interface Tour {
  id: number;
  title: string;
  description: string;
  price: number;
  imageUrl: string;
  destination: string;
  tourType: string;
  transport: string;
  duration: number;
  maxPeople: number;
  rating?: number;
  reviewCount?: number;
}

export interface ToursResponse {
  content: Tour[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

interface FetchToursParams {
  page?: number;
  size?: number;
  keyword?: string;
  destination?: string;
  tourType?: string;
  transport?: string;
  minPrice?: number;
  maxPrice?: number;
  sortBy?: string;
  sortDir?: string;
}

export const fetchTours = async (params: FetchToursParams): Promise<ToursResponse> => {
  const queryParams = new URLSearchParams();
  if (params.page !== undefined) queryParams.append('page', params.page.toString());
  if (params.size !== undefined) queryParams.append('size', params.size.toString());
  if (params.keyword) queryParams.append('keyword', params.keyword);
  if (params.destination) queryParams.append('destination', params.destination);
  if (params.tourType) queryParams.append('tourType', params.tourType);
  if (params.transport) queryParams.append('transport', params.transport);
  if (params.minPrice !== undefined) queryParams.append('minPrice', params.minPrice.toString());
  if (params.maxPrice !== undefined) queryParams.append('maxPrice', params.maxPrice.toString());
  if (params.sortBy) queryParams.append('sortBy', params.sortBy);
  if (params.sortDir) queryParams.append('sortDir', params.sortDir);

  const response = await api.get(`/tours/search?${queryParams.toString()}`);
  return response.data?.data || response.data;
};

export const useTours = (params: FetchToursParams) => {
  return useQuery({
    queryKey: ['tours', params],
    queryFn: () => fetchTours(params),
    // Keep previous data while fetching new pages to avoid UI flickering
    placeholderData: (previousData) => previousData,
  });
};
