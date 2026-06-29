export interface ProfileCreateRequest {
  username: string;
  firstName?: string;
  lastName?: string;
  birthDate?: string;
  bio?: string;
}

export interface ProfileUpdateRequest {
  firstName?: string;
  lastName?: string;
  birthDate?: string;
  bio?: string;
}
