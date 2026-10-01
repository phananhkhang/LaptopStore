export const ROLES = {
  USER: 'ROLE_USER',
  STAFF: 'ROLE_STAFF',
  ADMIN: 'ROLE_ADMIN',
} as const;

export type Role = (typeof ROLES)[keyof typeof ROLES];
