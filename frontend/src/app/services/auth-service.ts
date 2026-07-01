import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../models/auth/login-request';
import { LoginRespone } from '../models/auth/login-response';
import { finalize, Observable, tap } from 'rxjs';
import { RegisterRequest } from '../models/auth/register-request';
import { BasicUser } from '../models/user/user-response';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly jwtTokenKey = 'token';
  private readonly apiUrl = environment.apiUrl;
  private readonly httpClient = inject(HttpClient);

  public register(body: RegisterRequest): Observable<BasicUser> {
    return this.httpClient.post<BasicUser>(`${this.apiUrl}/auth/register`, body);
  }

  public login(body: LoginRequest): Observable<LoginRespone> {
    return this.httpClient
      .post<LoginRespone>(`${this.apiUrl}/auth/login`, body, { withCredentials: true })
      .pipe(
        tap((res) => {
          localStorage.setItem(this.jwtTokenKey, res.token);
        }),
      );
  }

  public refresh(): Observable<LoginRespone> {
    return this.httpClient
      .post<LoginRespone>(`${this.apiUrl}/auth/refresh`, {}, { withCredentials: true })
      .pipe(tap((res) => localStorage.setItem(this.jwtTokenKey, res.token)));
  }

  public logout(): Observable<void> {
    return this.httpClient
      .post<void>(`${this.apiUrl}/auth/logout`, {}, { withCredentials: true })
      .pipe(finalize(() => localStorage.removeItem(this.jwtTokenKey)));
  }

  public isAuthenticated(): boolean {
    const token = localStorage.getItem(this.jwtTokenKey);
    return !!token;
  }
}
