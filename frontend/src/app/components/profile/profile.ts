import { Component, inject, signal } from '@angular/core';
import { UserService } from '../../services/user-service';
import { DetailedUser } from '../../models/auth/user-response';
import { OnInit } from '@angular/core';

@Component({
  selector: 'app-profile',
  imports: [],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {

  private readonly userService = inject(UserService);
  public user = signal<DetailedUser | null>(null);

  ngOnInit() {
      this.userService.getProfile().subscribe(
        res => this.user.set(res)
      );
  }
}
