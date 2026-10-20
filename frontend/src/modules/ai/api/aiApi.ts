import { apiClient } from '@/services/api/apiClient';
import { AIChatRequest, AIChatResponse } from '../types';

export const aiApi = {
  chat: async (request: AIChatRequest): Promise<AIChatResponse> => {
    const response = await apiClient.post<AIChatResponse>('/ai/chat', request);
    return response.data;
  },
};
