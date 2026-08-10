import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DeviceService } from '../../../../services/device-service';
import { DeviceUpdateRequest } from '../../../../models/device/device-request';
import { DEVICE_TYPE_LABELS, DeviceType } from '../../../../models/device/device-type';
import { ipv4Validator } from '../../../../shared/validators/ip.validator';
import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { DeviceResponse } from '../../../../models/device/device-response';

@Component({
  selector: 'app-update-device',
  imports: [ReactiveFormsModule],
  templateUrl: './update-device.html',
  styleUrl: './update-device.scss',
})
export class UpdateDevice {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(DialogRef);
  private readonly deviceService = inject(DeviceService);
  private readonly device = inject<DeviceResponse>(DIALOG_DATA);

  protected readonly deviceTypes = Object.values(DeviceType);
  protected readonly DEVICE_TYPE_LABELS = DEVICE_TYPE_LABELS;

  protected readonly errorMessage = signal('');
  protected readonly deviceUpdateForm = this.fb.nonNullable.group({
    name: [this.device.name, [Validators.required, Validators.maxLength(100)]],
    ipAddress: [this.device.ipAddress, [Validators.required, ipv4Validator()]],
    type: [this.device.type, Validators.required],
  });

  protected submit(): void {
    if (this.deviceUpdateForm.invalid) {
      this.deviceUpdateForm.markAllAsTouched();
      return;
    }

    const request: DeviceUpdateRequest = this.deviceUpdateForm.getRawValue();

    this.deviceService.updateDevice(request, this.device.id).subscribe({
      next: () => this.dialogRef.close({ updated: true }),
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while updating the device.'),
    });
  }
}
