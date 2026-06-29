import { Component, inject, signal } from '@angular/core';
import { UserService } from '../../services/user-service';
import {DetailedUser, UserProfile} from '../../models/user/user-response';
import { OnInit } from '@angular/core';
import {DatePipe} from '@angular/common';
import {Dialog, DialogModule} from '@angular/cdk/dialog';
import {ProfileDialog} from './profile-dialog/profile-dialog';

@Component({
  selector: 'app-profile',
  imports: [DatePipe, DialogModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {

  private readonly userService = inject(UserService);
  private readonly dialog = inject(Dialog);
  public user = signal<DetailedUser | null>(null);

  ngOnInit() {
      this.userService.getProfile().subscribe(
        res => this.user.set(res)
      );
  }

  public openCreateProfile() {
    const ref = this.dialog.open<UserProfile>(ProfileDialog);

    ref.closed.subscribe(createdProfile => {
      if (!createdProfile) return;

      this.user.update(actual => {
        if (!actual) return actual;
        return {
          ...actual,
          profile: createdProfile
        }
      })
    });
  }
}
