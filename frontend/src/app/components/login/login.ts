import {Component, inject, signal} from '@angular/core';
import { AuthService } from '../../services/auth-service';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LoginRequest } from '../../models/auth/login-request';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

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
          console.log(err)
        }
    });
  }
}
