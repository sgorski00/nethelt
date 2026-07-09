import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RegisterRequest } from '../../models/auth/register-request';
import { AuthService } from '../../services/auth-service';
import { Router } from '@angular/router';
import { passwordMatchValidator } from '../../shared/validators/password-match.validator';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  public readonly errorMessage = signal('');
  public readonly registerForm = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      repeatNewPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator },
  );

  public register() {
    if (!this.registerForm.valid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const request: RegisterRequest = this.registerForm.getRawValue();

    this.authService.register(request).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: true } }),
      error: (err) =>
        this.errorMessage.set(err.error.detail || 'An error occurred during registration.'),
    });
  }
}
