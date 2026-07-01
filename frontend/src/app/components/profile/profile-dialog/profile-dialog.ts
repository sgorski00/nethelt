import { Component, inject, OnInit, signal } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { ProfileCreateRequest, ProfileUpdateRequest } from '../../../models/user/profile-request';
import { UserService } from '../../../services/user-service';
import { UserProfile } from '../../../models/user/user-response';
import { ProfileDialogData } from './profile-dialog-data';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-profile-dialog',
  imports: [ReactiveFormsModule, FormsModule, DatePipe],
  templateUrl: './profile-dialog.html',
  styleUrl: './profile-dialog.scss',
})
export class ProfileDialog implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(DialogRef<UserProfile>);
  private readonly fb = inject(FormBuilder);
  private readonly data = inject<ProfileDialogData>(DIALOG_DATA);

  public readonly isEdit = this.data.mode === 'update';
  public readonly today = new Date();
  public readonly errorMessage = signal('');
  public readonly profileForm: FormGroup = this.fb.group({
    username: this.fb.nonNullable.control('', [Validators.required]),
    firstName: this.fb.control<string | null>(null),
    lastName: this.fb.control<string | null>(null),
    birthDate: this.fb.control<string | null>(null),
    bio: this.fb.control<string | null>(null),
  });

  ngOnInit(): void {
    if (this.isEdit && this.data.profile) {
      this.profileForm.patchValue({
        username: this.data.profile!.username,
        firstName: this.data.profile!.firstName,
        lastName: this.data.profile!.lastName,
        birthDate: this.data.profile!.birthDate,
        bio: this.data.profile!.bio,
      });
      this.profileForm.get('username')!.disable();
    }
  }

  public closeDialog() {
    this.dialogRef.close();
  }

  public submit() {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    if (this.isEdit) {
      this.updateProfile();
    } else {
      this.createProfile();
    }
  }

  private updateProfile() {
    const profileData: ProfileUpdateRequest = this.profileForm.getRawValue();
    if (!this.data.profile) {
      this.errorMessage.set('Nie można zaktualizować profilu, brak danych.');
      return;
    }
    this.userService.updateProfile(profileData).subscribe({
      next: (updatedProfile) => {
        this.dialogRef.close(updatedProfile);
      },
      error: (err) => this.errorMessage.set(err.error.detail || 'Nie udało się zaktualizować.'),
    });
  }

  private createProfile() {
    const profileData: ProfileCreateRequest = this.profileForm.getRawValue();
    this.userService.createProfile(profileData).subscribe({
      next: (createdProfile) => {
        this.dialogRef.close(createdProfile);
      },
      error: (err) => this.errorMessage.set(err.error.detail || 'Nie udało się zapisać.'),
    });
  }
}
