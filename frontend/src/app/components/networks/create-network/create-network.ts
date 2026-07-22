import { Component, inject, signal } from '@angular/core';
import { NetworkService } from '../../../services/network-service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NetworkRequest } from '../../../models/network/network-request';

@Component({
  selector: 'app-create-network',
  imports: [ReactiveFormsModule],
  templateUrl: './create-network.html',
  styleUrl: './create-network.scss',
})
export class CreateNetwork {
  private readonly fb = inject(FormBuilder);
  private readonly networkService = inject(NetworkService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal('');
  protected readonly networkCreateForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    description: [''],
  });

  protected submit(): void {
    if (this.networkCreateForm.invalid) {
      this.networkCreateForm.markAllAsTouched();
      return;
    }

    const request: NetworkRequest = this.networkCreateForm.getRawValue();

    this.networkService.createNetwork(request).subscribe({
      next: (network) => {
        this.router.navigate(['/networks', network.id]);
      },
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while creating the network.'),
    });
  }
}
