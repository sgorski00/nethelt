export enum DeviceType {
  LAN_CLIENT = 'LAN_CLIENT',
  WIFI_CLIENT = 'WIFI_CLIENT',
  NETWORK_DEVICE = 'NETWORK_DEVICE',
}

export const DEVICE_TYPE_LABELS: Record<DeviceType, string> = {
  [DeviceType.LAN_CLIENT]: 'LAN Client',
  [DeviceType.WIFI_CLIENT]: 'WiFi Client',
  [DeviceType.NETWORK_DEVICE]: 'Network Infrastructure',
};
