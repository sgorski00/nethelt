import { Component, inject, signal } from '@angular/core';
import { NetworkService } from '../../../services/network-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { Dialog } from '@angular/cdk/dialog';
import { ConfirmDeleteNetworkDialog } from './confirm-delete-network-dialog/confirm-delete-network-dialog';

@Component({
  selector: 'app-single-network',
  imports: [DatePipe],
  templateUrl: './network-details.html',
  styleUrl: './network-details.scss',
})
export class NetworkDetails {
  private readonly dialog = inject(Dialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly networkService = inject(NetworkService);

  protected readonly errorMessage = signal('');
  protected readonly network = toSignal(
    this.route.paramMap.pipe(
      switchMap((params) => this.networkService.getNetwork(Number(params.get('id')))),
    ),
  );

  protected edit() {
    //todo: implement with reload cache
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
}
