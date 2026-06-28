import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { LoginRequest } from '../models/auth/login-request';
import { LoginRespone } from '../models/auth/login-response';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  
  private readonly apiUrl = environment.apiUrl;
  private readonly httpClient = inject(HttpClient);

  public login(body: LoginRequest): Observable<LoginRespone> {
    return this.httpClient
      .post<LoginRespone>(`${this.apiUrl}/auth/login`, body)
      .pipe(
        tap(res => {
          localStorage.setItem('token', res.token)
        })
      );
  }

}
