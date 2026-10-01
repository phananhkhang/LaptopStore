export interface UserProfile {
  id: number;
  email: string;
  fullName?: string;
  phone?: string;
  status: string;
  roles: string[];
  createdAt: string;
}

export interface Address {
  id: number;
  userId: number;
  recipientName: string;
  phone: string;
  ward?: string;
  district?: string;
  city: string;
  street: string;
  postalCode?: string;
  isDefault: boolean;
  createdAt: string;
}

export interface UpdateProfileRequest {
  fullName?: string;
  phone?: string;
}

export interface CreateAddressRequest {
  recipientName: string;
  phone: string;
  ward?: string;
  district?: string;
  city: string;
  street: string;
  postalCode?: string;
  isDefault?: boolean;
}
