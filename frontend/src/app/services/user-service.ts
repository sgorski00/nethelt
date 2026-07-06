import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DetailedUser, UserProfile } from '../models/user/user-response';
import { ProfileCreateRequest, ProfileUpdateRequest } from '../models/user/profile-request';
import { IdentityProvider } from '../models/user/identity-provider';
import { PasswordChangeRequest, PasswordSetRequest } from '../models/user/password-request';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = environment.apiUrl;
  private readonly httpClient = inject(HttpClient);

  public getProfile(): Observable<DetailedUser> {
    return this.httpClient.get<DetailedUser>(`${this.apiUrl}/profile`);
  }

  public createProfile(profileData: ProfileCreateRequest): Observable<UserProfile> {
    return this.httpClient.post<UserProfile>(`${this.apiUrl}/profile`, profileData);
  }

  public updateProfile(profileData: ProfileUpdateRequest): Observable<UserProfile> {
    return this.httpClient.put<UserProfile>(`${this.apiUrl}/profile`, profileData);
  }

  public linkAccount(provider: IdentityProvider) {
    const providerStr = provider.toLocaleLowerCase();
    this.httpClient
      .post(`${this.apiUrl}/identities/${providerStr}/link`, {}, { withCredentials: true })
      .subscribe({
        next: () => (window.location.href = `${this.apiUrl}/oauth2/authorization/${providerStr}`),
      });
  }

  public changePassword(request: PasswordChangeRequest): Observable<void> {
    return this.httpClient.patch<void>(`${this.apiUrl}/profile/password`, request, {
      withCredentials: true,
    });
  }

  public setPassword(request: PasswordSetRequest): Observable<void> {
    return this.httpClient.put<void>(`${this.apiUrl}/profile/password`, request, {
      withCredentials: true,
    });
  }
}
