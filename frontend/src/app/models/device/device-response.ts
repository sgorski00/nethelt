import { DeviceType } from './device-type';

export interface DeviceResponse {
  id: number;
  name: string;
  ipAddress: string;
  type: DeviceType;
  isEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}
