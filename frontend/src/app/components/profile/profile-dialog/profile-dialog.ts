import {Component, inject, signal} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {DialogRef} from '@angular/cdk/dialog';
import {ProfileCreateRequest} from '../../../models/user/profile-create-request';
import {UserService} from '../../../services/user-service';
import {UserProfile} from '../../../models/user/user-response';

@Component({
  selector: 'app-profile-dialog',
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './profile-dialog.html',
  styleUrl: './profile-dialog.scss',
})
export class ProfileDialog {

  private readonly userService = inject(UserService);
  private readonly dialogRef = inject(DialogRef<UserProfile>);
  private readonly fb = inject(FormBuilder);

  public readonly today = new Date();
  public readonly errorMessage = signal('');
  public readonly createProfileForm: FormGroup = this.fb.group({
    username: this.fb.nonNullable.control('', [Validators.required]),
    firstName: this.fb.control<string | null>(null),
    lastName: this.fb.control<string | null>(null),
    birthDate: this.fb.control<string | null>(null),
    bio: this.fb.control<string | null>(null),
  })

  public closeDialog() {
    this.dialogRef.close();
  }

  public createProfile(): void {
    if(this.createProfileForm.invalid) {
      this.createProfileForm.markAllAsTouched();
      return;
    }

    const profileData: ProfileCreateRequest = this.createProfileForm.getRawValue();

    this.userService.createProfile(profileData).subscribe({
      next: createdProfile => {
        this.dialogRef.close(createdProfile);
      },
      error: err => this.errorMessage.set(err.error.detail || 'Nie udało się zapisać.'),
    });
  }
}
