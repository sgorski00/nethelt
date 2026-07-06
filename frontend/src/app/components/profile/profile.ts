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
import { OAUTH2_ERRORS } from '../oauth2-callback/oauth2-errors';

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
  public error = signal('');
  public user = signal<DetailedUser | null>(null);

  ngOnInit() {
    this.reloadUser();
    const errorCode = this.route.snapshot.queryParamMap.get('error');
    switch (errorCode) {
      case OAUTH2_ERRORS.OAUTH2_LINK_ERROR:
        this.error.set('Failed to link social media account. Please try again.');
        break;
    }
  }

  public openCreateProfile() {
    const ref = this.dialog.open<UserProfile>(ProfileDialog, { data: { mode: 'create' } });
    this.updateProfileView(ref);
  }

  public openUpdateProfile() {
    const ref = this.dialog.open<UserProfile>(ProfileDialog, {
      data: { mode: 'update', profile: this.user()!.profile },
    });
    this.updateProfileView(ref);
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
      error: (err) => this.error.set(`Failed to unlink ${provider} account: ${err.error.detail}`),
    });
  }

  public openPasswordDialog() {
    const mode = this.user()?.hasPasswordSet ? 'change' : 'set';
    this.dialog
      .open<boolean>(PasswordDialog, { data: { mode: mode } })
      .closed.subscribe((isChanged) => {
        if (isChanged) {
          this.reloadUser();
          this.message.set('Password updated successfully');
        }
      });
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

  private reloadUser() {
    this.userService.getProfile().subscribe((res) => this.user.set(res));
  }
}
