import {
  apiClient,
  setAccessToken,
  refreshAccessToken,
} from "@/services/api/apiClient";
import {
  AuthResponse,
  LoginRequest,
  RefreshTokenResponse,
  RegisterRequest,
} from "../types";

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(
      "/auth/login",
      credentials,
    );
    setAccessToken(response.data.accessToken);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>("/auth/register", data);
    setAccessToken(response.data.accessToken);
    return response.data;
  },

  refresh: async (): Promise<RefreshTokenResponse> => {
    return refreshAccessToken();
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient.post("/auth/logout");
    } finally {
      setAccessToken(null);
    }
  },
};
