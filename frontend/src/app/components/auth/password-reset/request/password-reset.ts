import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../../services/auth-service';
import { PasswordResetRequest } from '../../../../models/auth/password-reset-request';

@Component({
  selector: 'app-password-reset',
  imports: [FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './password-reset.html',
  styleUrl: './password-reset.scss',
})
export class PasswordReset {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  public readonly errorMessage = signal('');
  public readonly success = signal(false);
  public readonly passwordResetForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  public sendRequest() {
    if (this.passwordResetForm.invalid) {
      this.passwordResetForm.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.success.set(false);
    const request: PasswordResetRequest = this.passwordResetForm.getRawValue();

    this.authService.requestPasswordReset(request).subscribe({
      next: () => this.success.set(true),
      error: (err) =>
        this.errorMessage.set(
          err.error?.detail || 'An error occurred while requesting password reset.',
        ),
    });
  }
}
