import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { NetworkService } from '../../../services/network-service';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { Dialog } from '@angular/cdk/dialog';
import { ConfirmDeleteNetworkDialog } from './confirm-delete-network-dialog/confirm-delete-network-dialog';
import { NetworkResponse } from '../../../models/network/network-response';

@Component({
  selector: 'app-single-network',
  imports: [DatePipe],
  templateUrl: './network-details.html',
  styleUrl: './network-details.scss',
})
export class NetworkDetails implements OnInit {
  private readonly dialog = inject(Dialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly networkService = inject(NetworkService);

  protected readonly editing = signal(false);
  protected readonly name = signal('');
  protected readonly description = signal('');

  protected readonly changed = computed(() => {
    const network = this.network();
    return (
      network &&
      (network.name !== this.name() || (network.description ?? '') !== this.description())
    );
  });

  protected readonly errorMessage = signal('');
  protected readonly network = signal<NetworkResponse | undefined>(undefined);

  ngOnInit(): void {
    this.loadNetwork();
  }

  protected edit() {
    const network = this.network();
    if (!network) return;

    this.name.set(network.name);
    this.description.set(network.description ?? '');
    this.editing.set(true);
  }

  protected cancelEdit() {
    this.editing.set(false);
  }

  protected save() {
    const network = this.network();
    if (!network) return;

    this.networkService
      .updateNetwork(network.id, {
        name: this.name(),
        description: this.description(),
      })
      .subscribe({
        next: (updated) => {
          this.editing.set(false);
          this.network.set(updated);
        },
        error: (err) => this.errorMessage.set(`Failed to update network: ${err.message}`),
      });
  }

  protected delete() {
    this.dialog
      .open(ConfirmDeleteNetworkDialog, {
        data: {
          networkName: this.network()!.name,
        },
      })
      .closed.subscribe((confirmed) => {
        if (!confirmed) return;

        this.networkService.deleteNetwork(this.network()!.id).subscribe({
          next: () => this.router.navigate(['/networks'], { queryParams: { deleted: true } }),
          error: (err) => this.errorMessage.set(`Failed to delete network: ${err.message}`),
        });
      });
  }

  private loadNetwork() {
    this.route.paramMap
      .pipe(switchMap((params) => this.networkService.getNetwork(Number(params.get('id')))))
      .subscribe({
        next: (network) => this.network.set(network),
        error: (err) => this.errorMessage.set(`Failed to load network: ${err.message}`),
      });
  }
}
