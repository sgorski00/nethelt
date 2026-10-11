import { DeviceType } from './device-type';

export interface DeviceFilter {
  name?: string;
  type?: DeviceType;
  ip?: string;
}
