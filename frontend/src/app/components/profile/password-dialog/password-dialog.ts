import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserService } from '../../../services/user-service';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { PasswordDialogData } from './password-dialog-data';
import { PasswordChangeRequest, PasswordSetRequest } from '../../../models/user/password-request';

@Component({
  selector: 'app-password-dialog',
  imports: [ReactiveFormsModule],
  templateUrl: './password-dialog.html',
  styleUrl: './password-dialog.scss',
})
export class PasswordDialog implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(DialogRef<boolean>);
  private readonly fb = inject(FormBuilder);
  private readonly data = inject<PasswordDialogData>(DIALOG_DATA);

  public errorMessage = signal('');
  public readonly isSetPasswordMode = this.data.mode === 'set';
  public readonly passwordForm = this.fb.group({
    oldPassword: this.fb.nonNullable.control('', [Validators.required]),
    newPassword: this.fb.nonNullable.control('', [Validators.required]),
    repeatNewPassword: this.fb.nonNullable.control('', [Validators.required]),
  });

  ngOnInit(): void {
    if (this.isSetPasswordMode) {
      this.passwordForm.controls.oldPassword.disable();
    }
  }

  public closeDialog() {
    this.dialogRef.close(false);
  }

  public submit() {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    if (this.isSetPasswordMode) {
      const passwordRequest: PasswordSetRequest = this.passwordForm.getRawValue();
      this.userService.setPassword(passwordRequest).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.errorMessage.set(err.error.detail || 'Failed to set password');
        },
      });
    } else {
      const passwordRequest: PasswordChangeRequest = this.passwordForm.getRawValue();
      this.userService.changePassword(passwordRequest).subscribe({
        next: () => {
          this.dialogRef.close(true);
        },
        error: (err) => {
          this.errorMessage.set(err.error.detail || 'Failed to change password');
        },
      });
    }
  }
}
