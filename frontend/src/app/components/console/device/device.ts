import { Component, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { DeviceService } from '../../../services/device-service';
import { Dialog } from '@angular/cdk/dialog';
import { CreateDevice } from './create-device/create-device';

@Component({
  selector: 'app-device',
  imports: [],
  templateUrl: './device.html',
  styleUrl: './device.scss',
})
export class Device {
  private readonly deviceService = inject(DeviceService);
  private readonly dialog = inject(Dialog);

  protected readonly devices = toSignal(this.deviceService.getDevices());

  protected openCreateDialog() {
    this.dialog.open(CreateDevice);
  }
}
