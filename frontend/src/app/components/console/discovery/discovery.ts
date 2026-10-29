import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { filter, finalize } from 'rxjs';
import { AgentService } from '../../../services/agent-service';
import { DiscoveryScanService } from '../../../services/discovery-scan-service';
import { WebSocketService } from '../../../services/websocket-service';
import { NetworkContextService } from '../../../services/network-context-service';
import { AgentResponse } from '../../../models/agent/agent-response';
import { AgentStatus } from '../../../models/agent/agent-status';
import { AgentConnectionState } from '../../../models/agent/agent-connection-state';
import { AgentConnectionStateResponse } from '../../../models/agent/agent-connection-state-response';
import {
  DiscoveryScanResponse,
  DiscoveryScanStatusResponse,
} from '../../../models/discovery/discovery-scan-response';
import {
  DISCOVERY_SCAN_STATUS_LABELS,
  DiscoveryScanStatus,
  isScanInProgress,
} from '../../../models/discovery/discovery-scan-status';
import { validateDiscoveryRange } from '../../../shared/validators/discovery-range.validator';

@Component({
  selector: 'app-discovery',
  imports: [DatePipe],
  templateUrl: './discovery.html',
  styles: `
    @use 'breakpoints' as bp;

    input {
      min-width: 260px;

      &.invalid {
        border-color: var(--color-danger);
      }

      @include bp.sm-down {
        min-width: 0;
      }
    }
  `,
})
export class Discovery implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly agentService = inject(AgentService);
  private readonly discoveryScanService = inject(DiscoveryScanService);
  private readonly webSocketService = inject(WebSocketService);
  private readonly networkContextService = inject(NetworkContextService);
  protected readonly DiscoveryScanStatus = DiscoveryScanStatus;
  protected readonly STATUS_LABELS = DISCOVERY_SCAN_STATUS_LABELS;

  protected readonly errorMessage = signal('');
  protected readonly agent = signal<AgentResponse | undefined>(undefined);
  protected readonly scan = signal<DiscoveryScanResponse | null | undefined>(undefined);
  protected readonly starting = signal(false);
  protected readonly ipRange = signal('');
  protected readonly ipRangeTouched = signal(false);
  protected readonly ipRangeError = computed(() =>
    this.ipRange().trim() ? validateDiscoveryRange(this.ipRange()) : null,
  );
  protected readonly agentOnline = computed(
    () =>
      this.agent()?.status === AgentStatus.ACTIVE &&
      this.agent()?.connectionState === AgentConnectionState.ONLINE,
  );
  protected readonly inProgress = computed(() => {
    const scan = this.scan();
    return !!scan && isScanInProgress(scan.status);
  });
  protected readonly canStart = computed(
    () => this.agentOnline() && !this.inProgress() && !this.starting(),
  );

  ngOnInit(): void {
    this.loadAgent();
    this.loadScan();
    this.webSocketService
      .watch<AgentConnectionStateResponse>('/user/queue/agents')
      .pipe(
        filter((state) => state.networkId === this.networkContextService.getActiveNetworkId()),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.loadAgent());
    this.webSocketService
      .watch<DiscoveryScanStatusResponse>('/user/queue/discovery-scans')
      .pipe(
        filter((status) => status.networkId === this.networkContextService.getActiveNetworkId()),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => this.loadScan());
  }

  protected onIpRangeInput(event: Event): void {
    this.ipRange.set((event.target as HTMLInputElement).value);
  }

  protected start(): void {
    this.ipRangeTouched.set(true);
    if (!this.canStart() || this.ipRangeError()) return;

    this.errorMessage.set('');
    this.starting.set(true);
    this.discoveryScanService
      .startScan({ ipRange: this.ipRange().trim() || null })
      .pipe(finalize(() => this.starting.set(false)))
      .subscribe({
        next: (scan) => this.scan.set(scan),
        error: (err) =>
          this.errorMessage.set(err.error?.detail || 'An error occurred while starting the scan.'),
      });
  }

  private loadAgent(): void {
    this.agentService.getAgent().subscribe({
      next: (agent) => this.agent.set(agent),
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while loading the agent.'),
    });
  }

  private loadScan(): void {
    this.discoveryScanService.getScan().subscribe({
      next: (scan) => this.scan.set(scan),
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while loading the scan.'),
    });
  }
}
