export interface UserSession {
  userId: number;
  email: string;
  fullName?: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  userId: number;
  email: string;
  fullName?: string;
  roles: string[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  phone?: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
}
