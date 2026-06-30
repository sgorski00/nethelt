import {Component, inject, signal} from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LoginRequest } from '../../models/auth/login-request';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import { environment } from '../../../environments/environment';
import { IdentityProvider } from '../../models/user/identity-provider';
import {toSignal} from '@angular/core/rxjs-interop';
import {map} from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly IdentityProvider = IdentityProvider;

  public readonly registered = toSignal(
    this.route.queryParams.pipe(
      map(params => params['registered'] === 'true')
    ),
    {initialValue: false}
  )
  public readonly errorMessage = signal('');
  public readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  })

  public login() {
    if(this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const request: LoginRequest = this.loginForm.getRawValue();

    this.authService.login(request).subscribe({
        next: () => this.router.navigateByUrl('/profile'),
        error: err => {
          this.errorMessage.set(err.error.detail || 'An error occurred during login.');
          console.log(err.error)
        }
    });
  }

  public loginWithProvider(provider: IdentityProvider) {
    window.location.href = `${environment.apiUrl}/oauth2/authorization/${provider.toLocaleLowerCase()}`
  }
}
