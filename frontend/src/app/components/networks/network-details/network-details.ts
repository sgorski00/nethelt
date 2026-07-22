import { Component, inject } from '@angular/core';
import { NetworkService } from '../../../services/network-service';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-single-network',
  imports: [DatePipe],
  templateUrl: './network-details.html',
  styleUrl: './network-details.scss',
})
export class NetworkDetails {
  private readonly route = inject(ActivatedRoute);
  private readonly networkService = inject(NetworkService);

  protected readonly network = toSignal(
    this.route.paramMap.pipe(
      switchMap((params) => this.networkService.getNetwork(Number(params.get('id')))),
    ),
  );

  protected editNetwork() {
    //todo: implement with reload cache
  }

  protected deleteNetwork() {
    //todo: implement with reload cache
  }
}
