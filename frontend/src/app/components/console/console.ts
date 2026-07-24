import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-console',
  imports: [RouterLink, RouterOutlet, RouterLinkActive],
  templateUrl: './console.html',
  styleUrl: './console.scss',
})
export class Console {
  protected readonly message = signal('');
}
