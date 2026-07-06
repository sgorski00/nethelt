import { IdentityProvider } from './identity-provider';

export interface BasicUser {
  email: string;
  role: string;
}

export interface DetailedUser {
  id: number;
  email: string;
  role: string;
  profile: UserProfile | null;
  identities: UserIdentity[];
  hasPasswordSet: boolean;
  createdAt: string;
  updatedAt: string;
  deletedAt: string | null;
}

export interface UserIdentity {
  provider: IdentityProvider;
  providerId: string;
}

export interface UserProfile {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  birthDate: string | null;
  bio: string | null;
  createdAt: string;
  updatedAt: string;
}
