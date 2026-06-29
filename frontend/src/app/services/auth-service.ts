import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../models/auth/login-request';
import { LoginRespone } from '../models/auth/login-response';
import {finalize, Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly jwtTokenKey = 'token';
  private readonly apiUrl = environment.apiUrl;
  private readonly httpClient = inject(HttpClient);

  public login(body: LoginRequest): Observable<LoginRespone> {
    return this.httpClient
      .post<LoginRespone>(`${this.apiUrl}/auth/login`, body, {withCredentials: true})
      .pipe(
        tap(res => {
          localStorage.setItem(this.jwtTokenKey, res.token)
        })
      );
  }

  public isAuthenticated(): boolean {
    const token = localStorage.getItem(this.jwtTokenKey);
    return !!token;
  }

  public logout(): Observable<void> {
    return this.httpClient.post<void>(`${this.apiUrl}/auth/logout`, {}, {withCredentials: true}).pipe(
      finalize(() => localStorage.removeItem(this.jwtTokenKey))
    )
  }

  public refresh(): Observable<LoginRespone> {
    return this.httpClient.post<LoginRespone>(`${this.apiUrl}/auth/refresh`, {}, {withCredentials: true}).pipe(
      tap(res => localStorage.setItem(this.jwtTokenKey, res.token))
    );
  }
}
