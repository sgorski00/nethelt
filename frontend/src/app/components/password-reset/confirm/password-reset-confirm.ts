import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth-service';
import { PasswordResetConfirmRequest } from '../../../models/auth/password-reset-request';
import { passwordMatchValidator } from '../../../shared/validators/password-match.validator';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-password-reset',
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './password-reset-confirm.html',
  styleUrl: './password-reset-confirm.scss',
})
export class PasswordResetConfirm {
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  public readonly errorMessage = signal('');
  public readonly passwordResetConfirmForm = this.fb.nonNullable.group(
    {
      newPassword: this.fb.nonNullable.control('', [Validators.required]),
      repeatNewPassword: this.fb.nonNullable.control('', [Validators.required]),
    },
    { validators: passwordMatchValidator },
  );

  public sendRequest() {
    if (this.passwordResetConfirmForm.invalid) {
      this.passwordResetConfirmForm.markAllAsTouched();
      return;
    }

    const token = this.route.snapshot.queryParamMap.get('token') || '';
    const request: PasswordResetConfirmRequest = this.passwordResetConfirmForm.getRawValue();

    this.authService.confirmPasswordReset(request, token).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { reset: 'success' } }),
      error: (err) =>
        this.errorMessage.set(
          err.error?.detail || 'An error occurred while resetting the password.',
        ),
    });
  }
}
