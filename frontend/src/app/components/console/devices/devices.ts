import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { DeviceService } from '../../../services/device-service';
import { Dialog } from '@angular/cdk/dialog';
import { CreateDevice } from './create-device/create-device';
import { catchError, of, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { DEVICE_TYPE_LABELS, DeviceType } from '../../../models/device/device-type';
import { UpdateDevice } from './update-device/update-device';
import { DeviceResponse } from '../../../models/device/device-response';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';
import { Pagination } from '../../shared/pagination/pagination';
import { RouterLink } from '@angular/router';
import { validateIpRangeFilter } from '../../../shared/validators/ip-range-filter.validator';
import { FiltersPanel } from '../../shared/filters-panel/filters-panel';

@Component({
  selector: 'app-device',
  imports: [FiltersPanel, DatePipe, Pagination, RouterLink],
  templateUrl: './devices.html',
  styleUrl: './devices.scss',
})
export class Devices {
  private readonly deviceService = inject(DeviceService);
  private readonly dialog = inject(Dialog);

  protected readonly deviceTypes = Object.values(DeviceType);
  protected readonly DEVICE_TYPE_LABELS = DEVICE_TYPE_LABELS;
  protected readonly pageSizes = [6, 9, 12, 18, 36, 72];

  protected readonly errorMessage = signal('');
  protected readonly message = signal('');

  private readonly reload = signal(0);
  protected readonly selectedType = signal<DeviceType | ''>('');
  protected readonly nameInput = signal('');
  protected readonly ipInput = signal('');
  protected readonly ipTouched = signal(false);
  protected readonly ipError = computed(() =>
    this.ipInput().trim() ? validateIpRangeFilter(this.ipInput()) : null,
  );
  private readonly name = signal('');
  private readonly ip = signal('');
  protected readonly hasFilters = computed(
    () => !!(this.nameInput() || this.ipInput() || this.name() || this.ip() || this.selectedType()),
  );
  protected readonly sort = signal('ipAddress,asc');
  protected readonly page = signal(0);
  protected readonly pageSize = signal(18);
  protected readonly devices = toSignal(
    toObservable(
      computed(() => ({
        filter: {
          name: this.name() || undefined,
          type: this.selectedType() || undefined,
          ip: this.ip() || undefined,
        },
        sort: this.sort(),
        page: this.page(),
        size: this.pageSize(),
        reload: this.reload(),
      })),
    ).pipe(
      switchMap(({ filter, sort, page, size }) =>
        this.deviceService.getDevices(filter, sort, page, size).pipe(
          catchError((err) => {
            this.errorMessage.set(`Failed to load devices: ${err.error?.detail ?? err.message}`);
            return of(null);
          }),
        ),
      ),
    ),
  );

  protected onNameInput(event: Event): void {
    this.nameInput.set((event.target as HTMLInputElement).value);
  }

  protected onIpInput(event: Event): void {
    this.ipInput.set((event.target as HTMLInputElement).value);
  }

  protected search(): void {
    this.ipTouched.set(true);
    if (this.ipError()) return;

    this.errorMessage.set('');
    this.name.set(this.nameInput().trim());
    this.ip.set(this.ipInput().trim());
    this.page.set(0);
  }

  protected clearFilters(): void {
    this.nameInput.set('');
    this.ipInput.set('');
    this.ipTouched.set(false);
    this.errorMessage.set('');
    this.name.set('');
    this.ip.set('');
    this.selectedType.set('');
    this.page.set(0);
  }

  protected onTypeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    this.selectedType.set(value as DeviceType | '');
    this.page.set(0);
  }

  protected onSortChange(event: Event): void {
    this.sort.set((event.target as HTMLSelectElement).value);
    this.page.set(0);
  }

  protected onPageChange(page: number): void {
    this.page.set(page);
  }

  protected onPageSizeChange(event: Event): void {
    const size = Number((event.target as HTMLSelectElement).value);

    this.pageSize.set(size);
    this.page.set(0);
  }

  protected openCreateDialog() {
    const dialogRef = this.dialog.open(CreateDevice);

    dialogRef.closed.subscribe((created) => {
      if (created) {
        this.message.set('Device created successfully.');
        this.reload.update((v) => v + 1);
      }
    });
  }

  protected openEditDialog(device: DeviceResponse) {
    const dialogRef = this.dialog.open(UpdateDevice, {
      data: device,
    });

    dialogRef.closed.subscribe((updated) => {
      if (updated) {
        this.message.set('Device updated successfully.');
        this.reload.update((v) => v + 1);
      }
    });
  }

  protected disable(id: number) {
    this.deviceService.disableDevice(id).subscribe({
      next: () => {
        this.message.set('Device disabled successfully.');
        this.reload.update((v) => v + 1);
      },
      error: (err) => this.errorMessage.set(`Failed to disable device: ${err.error?.detail}`),
    });
  }

  protected enable(id: number) {
    this.deviceService.enableDevice(id).subscribe({
      next: () => {
        this.message.set('Device enabled successfully.');
        this.reload.update((v) => v + 1);
      },
      error: (err) => this.errorMessage.set(`Failed to enable device: ${err.error?.detail}`),
    });
  }

  protected delete(device: DeviceResponse) {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: `Delete ${device.name}`,
          message: 'Are you sure you want to delete this device?',
          confirmText: device.name,
        },
      })
      .closed.subscribe((confirmed) => {
        if (!confirmed) return;

        this.deviceService.deleteDevice(device.id).subscribe({
          next: () => {
            this.message.set('Device deleted successfully.');
            this.reload.update((v) => v + 1);
          },
          error: (err) => this.errorMessage.set(`Failed to delete device: ${err.error?.detail}`),
        });
      });
  }
}
