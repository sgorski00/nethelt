import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {DetailedUser, UserProfile} from '../models/user/user-response';
import {ProfileCreateRequest} from '../models/user/profile-create-request';

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
}
