import { Component, inject, signal } from '@angular/core';
import { UserService } from '../../services/user-service';
import { DetailedUser, UserProfile } from '../../models/user/user-response';
import { OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Dialog, DialogModule, DialogRef } from '@angular/cdk/dialog';
import { ProfileDialog } from './profile-dialog/profile-dialog';
import { IdentityProvider } from '../../models/user/identity-provider';
import { hasIdentity } from '../../models/user/user.utils';
import { PasswordDialog } from './password-dialog/password-dialog';
import { ActivatedRoute } from '@angular/router';
import { getOAuth2ErrorMessage } from '../oauth2-callback/oauth2-errors';
import { PROFILE_DIALOG_MODE } from './profile-dialog/profile-dialog-data';
import { PASSWORD_DIALOG_MODE } from './password-dialog/password-dialog-data';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, DialogModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  private readonly dialog = inject(Dialog);
  protected readonly IdentityProvider = IdentityProvider;
  protected readonly hasIdentity = hasIdentity;

  public message = signal('');
  public errorMessage = signal('');
  public user = signal<DetailedUser | null>(null);

  ngOnInit() {
    this.reloadUser();
    const error = this.route.snapshot.queryParamMap.get('error');
    if (error) {
      this.errorMessage.set(getOAuth2ErrorMessage(error));
    }
  }

  public openCreateProfile() {
    const ref = this.dialog.open<UserProfile>(ProfileDialog, {
      data: { mode: PROFILE_DIALOG_MODE.CREATE },
    });
    this.updateProfileView(ref);
  }

  public openUpdateProfile() {
    const ref = this.dialog.open<UserProfile>(ProfileDialog, {
      data: { mode: PROFILE_DIALOG_MODE.UPDATE, profile: this.user()!.profile },
    });
    this.updateProfileView(ref);
  }

  private updateProfileView(ref: DialogRef<UserProfile>) {
    ref.closed.subscribe((newProfile) => {
      if (!newProfile) return;

      this.user.update((actualProfile) => {
        if (!actualProfile) return actualProfile;
        return {
          ...actualProfile,
          profile: newProfile,
        };
      });
    });
  }

  public linkSocialMediaAccount(provider: IdentityProvider) {
    this.userService.linkAccount(provider);
  }

  public unlinkSocialMediaAccount(provider: IdentityProvider) {
    this.userService.unlinkAccount(provider).subscribe({
      next: () => {
        this.reloadUser();
        this.message.set(`Successfully unlinked ${provider} account`);
      },
      error: (err) =>
        this.errorMessage.set(`Failed to unlink ${provider} account: ${err.error.detail}`),
    });
  }

  public openPasswordDialog() {
    const mode = this.user()?.isLocal ? PASSWORD_DIALOG_MODE.CHANGE : PASSWORD_DIALOG_MODE.SET;
    this.dialog
      .open<boolean>(PasswordDialog, { data: { mode: mode } })
      .closed.subscribe((isChanged) => {
        if (isChanged) {
          this.reloadUser();
          this.message.set('Password updated successfully');
        }
      });
  }

  private reloadUser() {
    this.userService.getProfile().subscribe((res) => this.user.set(res));
  }
}
