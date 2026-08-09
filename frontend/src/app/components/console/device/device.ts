import { Component, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { DeviceService } from '../../../services/device-service';
import { Dialog } from '@angular/cdk/dialog';
import { CreateDevice } from './create-device/create-device';
import { switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { DEVICE_TYPE_LABELS } from '../../../models/device/device-type';

@Component({
  selector: 'app-device',
  imports: [DatePipe],
  templateUrl: './device.html',
  styleUrl: './device.scss',
})
export class Device {
  private readonly deviceService = inject(DeviceService);
  private readonly dialog = inject(Dialog);

  protected readonly DEVICE_TYPE_LABELS = DEVICE_TYPE_LABELS;

  private readonly reload = signal(0);
  protected readonly message = signal('');
  protected readonly devices = toSignal(
    toObservable(this.reload).pipe(switchMap(() => this.deviceService.getDevices())),
  );

  protected openCreateDialog() {
    const dialogRef = this.dialog.open(CreateDevice);

    dialogRef.closed.subscribe((created) => {
      if (created) {
        this.message.set('Device created successfully.');
        this.reload.update((v) => v + 1);
      }
    });
  }
}
