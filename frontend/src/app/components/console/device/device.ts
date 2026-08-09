import { Component, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { DeviceService } from '../../../services/device-service';
import { Dialog } from '@angular/cdk/dialog';
import { CreateDevice } from './create-device/create-device';
import { switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { DEVICE_TYPE_LABELS, DeviceType } from '../../../models/device/device-type';
import { UpdateDevice } from './update-device/update-device';
import { DeviceResponse } from '../../../models/device/device-response';
import { ConfirmDialog } from '../../shared/confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-device',
  imports: [DatePipe],
  templateUrl: './device.html',
  styleUrl: './device.scss',
})
export class Device {
  private readonly deviceService = inject(DeviceService);
  private readonly dialog = inject(Dialog);

  protected readonly deviceTypes = Object.values(DeviceType);
  protected readonly DEVICE_TYPE_LABELS = DEVICE_TYPE_LABELS;

  private readonly reload = signal(0);
  protected readonly selectedType = signal<DeviceType | ''>('');
  protected readonly sort = signal('ipAddress,asc');
  //todo: add pagination
  protected readonly errorMessage = signal('');
  protected readonly message = signal('');
  protected readonly devices = toSignal(
    toObservable(this.reload).pipe(
      switchMap(() =>
        this.deviceService.getDevices(this.selectedType() || undefined, this.sort(), 0, 20),
      ),
    ),
  );

  protected onTypeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    this.selectedType.set(value as DeviceType | '');
    this.reload.update((v) => v + 1);
  }

  protected onSortChange(event: Event): void {
    this.sort.set((event.target as HTMLSelectElement).value);
    this.reload.update((v) => v + 1);
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
      error: (err) => this.errorMessage.set(`Failed to disable device: ${err.message}`),
    });
  }

  protected enable(id: number) {
    this.deviceService.enableDevice(id).subscribe({
      next: () => {
        this.message.set('Device enabled successfully.');
        this.reload.update((v) => v + 1);
      },
      error: (err) => this.errorMessage.set(`Failed to enable device: ${err.message}`),
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
          error: (err) => this.errorMessage.set(`Failed to delete device: ${err.message}`),
        });
      });
  }
}
