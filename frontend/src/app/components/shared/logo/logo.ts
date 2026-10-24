import { booleanAttribute, Component, input } from '@angular/core';

@Component({
  selector: 'app-logo',
  imports: [],
  templateUrl: './logo.html',
  styleUrl: './logo.scss',
})
export class Logo {
  readonly wordmark = input(false, { transform: booleanAttribute });
}
