import { DeviceType } from './device-type';

export interface DeviceCreateRequest {
  name: string;
  ipAddress: string;
  type: DeviceType;
}

export interface DeviceUpdateRequest {
  name: string;
  ipAddress: string;
  type: DeviceType;
}
