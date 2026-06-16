import { useQuery } from '@tanstack/react-query';
import api from '../api/axiosConfig';

export interface TourOptions {
  destinations: string[];
  tourTypes: string[];
  transports: string[];
}

export const fetchTourOptions = async (): Promise<TourOptions> => {
  const response = await api.get('/tours/options');
  return response.data;
};

export const useTourOptions = () => {
  return useQuery({
    queryKey: ['tourOptions'],
    queryFn: fetchTourOptions,
    staleTime: 10 * 60 * 1000, // 10 minutes cache
  });
};
