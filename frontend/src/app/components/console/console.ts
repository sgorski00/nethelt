import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

@Component({
  selector: 'app-console',
  imports: [RouterLink, RouterOutlet, RouterLinkActive],
  templateUrl: './console.html',
  styleUrl: './console.scss',
})
export class Console {
  private readonly route = inject(ActivatedRoute);

  protected readonly message = toSignal(
    this.route.queryParamMap.pipe(
      map((params) => (params.get('agentDeleted') ? 'Network Agent deleted successfully' : '')),
    ),
  );
}
