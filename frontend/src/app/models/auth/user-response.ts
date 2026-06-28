export interface DetailedUser {
  id: number,
  email: string,
  role: string,
  profile: Profile | null,
  identities: UserIdentity[],
  createdAt: string,
  updatedAt: string,
  deletedAt: string | null
}

export interface UserIdentity {
  provider: string,
  providerId: string
}

export interface Profile {
  id: number,
  username: string,
  firstName: string | null,
  lastName: string | null,
  birthDate: string | null,
  bio: string | null,
  createdAt: string,
  updatedAt: string
}