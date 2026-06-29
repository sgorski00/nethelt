import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {DetailedUser, UserProfile} from '../models/user/user-response';
import {ProfileCreateRequest, ProfileUpdateRequest} from '../models/user/profile-request';
import { IdentityProvider } from '../models/user/identity-provider';

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
    //todo: backend needs to be changed:
    // oauth endpoints should be permitAll()
    // the below enpoint should be post, not get
    // it will check jwt, save context in cookie and redirect 204. 
    // after that angular in the subscribe should use .href to redirect to /oauth2/authorization/provider with correct cookies
    this.httpClient.get(`${this.apiUrl}/profile/link/${provider}`).subscribe();
  }
}
