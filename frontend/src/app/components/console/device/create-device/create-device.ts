import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DeviceService } from '../../../../services/device-service';
import { DeviceCreateRequest } from '../../../../models/device/device-request';
import { DeviceType } from '../../../../models/device/device-type';
import { ipv4Validator } from '../../../../shared/validators/ip.validator';

@Component({
  selector: 'app-create-agent',
  imports: [ReactiveFormsModule],
  templateUrl: './create-device.html',
  styleUrl: './create-device.scss',
})
export class CreateDevice {
  private readonly fb = inject(FormBuilder);
  private readonly deviceService = inject(DeviceService);
  private readonly router = inject(Router);

  protected readonly errorMessage = signal('');
  protected readonly deviceCreateForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    ipAddress: ['', [Validators.required, ipv4Validator()]],
    type: [DeviceType.LAN_CLIENT, Validators.required],
  });

  protected submit(): void {
    if (this.deviceCreateForm.invalid) {
      this.deviceCreateForm.markAllAsTouched();
      return;
    }

    const request: DeviceCreateRequest = this.deviceCreateForm.getRawValue();

    this.deviceService.createDevice(request).subscribe({
      next: () => this.router.navigate(['/console/devices']), //todo: add success query param
      error: (err) =>
        this.errorMessage.set(err.error?.detail || 'An error occurred while creating the device.'),
    });
  }
}
