import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OAUTH2_ERRORS, OAuth2Error } from './oauth2-errors';

@Component({
  selector: 'app-oauth2-failure-callback',
  imports: [],
  template: '',
})
export class OAuth2FailureCallback implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  ngOnInit(): void {
    const error = this.route.snapshot.queryParamMap.get('error') as OAuth2Error | null;
    if (!error) {
      this.router.navigate(['/profile']);
      return;
    }
    switch (error) {
      case OAUTH2_ERRORS.ACCOUNT_LINK_REQUIRED:
        this.router.navigate(['/login'], { queryParams: { error } });
        break;
      default:
        this.router.navigate(['/profile'], { queryParams: { error } });
        return;
    }
  }
}
