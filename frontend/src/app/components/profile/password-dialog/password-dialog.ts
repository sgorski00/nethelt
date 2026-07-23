import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../services/user-service';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { PASSWORD_DIALOG_MODE, PasswordDialogData } from './password-dialog-data';
import { PasswordChangeRequest, PasswordSetRequest } from '../../../models/user/password-request';
import { Observable } from 'rxjs';
import { passwordMatchValidator } from '../../../shared/validators/password-match.validator';

@Component({
  selector: 'app-password-dialog',
  imports: [ReactiveFormsModule],
  templateUrl: './password-dialog.html',
})
export class PasswordDialog {
  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(DialogRef<boolean>, { optional: true });
  private readonly fb = inject(FormBuilder);
  private readonly data = inject<PasswordDialogData>(DIALOG_DATA);

  public errorMessage = signal('');
  public readonly isSetPasswordMode = this.data.mode === PASSWORD_DIALOG_MODE.SET;
  public readonly passwordForm = this.fb.group(
    {
      oldPassword: this.fb.nonNullable.control({ value: '', disabled: this.isSetPasswordMode }, [
        Validators.required,
      ]),
      newPassword: this.fb.nonNullable.control('', [Validators.required]),
      repeatNewPassword: this.fb.nonNullable.control('', [Validators.required]),
    },
    { validators: passwordMatchValidator },
  );

  public closeDialog() {
    this.dialogRef?.close(false);
  }

  public submit() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    if (this.isSetPasswordMode) {
      this.handleSetPassword();
    } else {
      this.handleChangePassword();
    }
  }

  private handleSetPassword() {
    const request: PasswordSetRequest = this.passwordForm.getRawValue();
    this.handlePasswordRequest(this.userService.setPassword(request));
  }

  private handleChangePassword() {
    const request: PasswordChangeRequest = this.passwordForm.getRawValue();
    this.handlePasswordRequest(this.userService.changePassword(request));
  }

  private handlePasswordRequest(request: Observable<void>) {
    request.subscribe({
      next: () => this.dialogRef?.close(true),
      error: (err) => this.errorMessage.set(err.error.detail || 'Failed to set/change password'),
    });
  }
}
