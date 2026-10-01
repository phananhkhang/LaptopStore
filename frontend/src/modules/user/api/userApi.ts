import { apiClient } from '@/services/api/apiClient';
import { Address, CreateAddressRequest, UpdateProfileRequest, UserProfile } from '../types';

export const userApi = {
  getProfile: async (): Promise<UserProfile> => {
    const response = await apiClient.get<UserProfile>('/me');
    return response.data;
  },

  updateProfile: async (data: UpdateProfileRequest): Promise<UserProfile> => {
    const response = await apiClient.patch<UserProfile>('/me', data);
    return response.data;
  },

  getAddresses: async (): Promise<Address[]> => {
    const response = await apiClient.get<Address[]>('/me/addresses');
    return response.data;
  },

  createAddress: async (data: CreateAddressRequest): Promise<Address> => {
    const response = await apiClient.post<Address>('/me/addresses', data);
    return response.data;
  },

  deleteAddress: async (id: number): Promise<void> => {
    await apiClient.delete(`/me/addresses/${id}`);
  },
};
