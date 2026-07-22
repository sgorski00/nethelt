import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { LoginRequest } from '../models/auth/login-request';
import { LoginResponse } from '../models/auth/login-response';
import { finalize, Observable, shareReplay, tap } from 'rxjs';
import { RegisterRequest } from '../models/auth/register-request';
import { BasicUser } from '../models/user/user-response';
import { Router } from '@angular/router';
import {
  PasswordResetConfirmRequest,
  PasswordResetRequest,
} from '../models/auth/password-reset-request';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly jwtTokenKey = 'token';
  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly httpClient = inject(HttpClient);
  private readonly router = inject(Router);

  private refreshTokenRequest$: Observable<LoginResponse> | null = null;

  public register(body: RegisterRequest): Observable<BasicUser> {
    return this.httpClient.post<BasicUser>(`${this.authUrl}/register`, body);
  }

  public login(body: LoginRequest): Observable<LoginResponse> {
    return this.httpClient
      .post<LoginResponse>(`${this.authUrl}/login`, body)
      .pipe(tap((res) => this.saveAccessToken(res.token)));
  }

  public refresh(): Observable<LoginResponse> {
    if (!this.refreshTokenRequest$) {
      this.refreshTokenRequest$ = this.httpClient
        .post<LoginResponse>(`${this.authUrl}/refresh`, {}, { withCredentials: true })
        .pipe(
          tap((res) => this.saveAccessToken(res.token)),
          finalize(() => (this.refreshTokenRequest$ = null)),
          shareReplay(1),
        );
    }
    return this.refreshTokenRequest$;
  }

  public logout(): Observable<void> {
    return this.httpClient
      .post<void>(`${this.authUrl}/logout`, {}, { withCredentials: true })
      .pipe(finalize(() => this.clearAccessToken()));
  }

  public requestPasswordReset(request: PasswordResetRequest) {
    return this.httpClient.post<void>(`${this.authUrl}/password-reset/request`, request);
  }

  public confirmPasswordReset(request: PasswordResetConfirmRequest, token: string) {
    const params = new HttpParams().set('token', token);
    return this.httpClient.post<void>(`${this.authUrl}/password-reset/confirm`, request, {
      params,
    });
  }

  public isAuthenticated(): boolean {
    return !!this.accessToken;
  }

  public handleUnauthorized(): void {
    this.clearAccessToken();
    this.router.navigate(['/login']);
  }

  public get accessToken(): string | null {
    return localStorage.getItem(this.jwtTokenKey);
  }

  private saveAccessToken(token: string): void {
    localStorage.setItem(this.jwtTokenKey, token);
  }

  private clearAccessToken(): void {
    localStorage.removeItem(this.jwtTokenKey);
  }
}
