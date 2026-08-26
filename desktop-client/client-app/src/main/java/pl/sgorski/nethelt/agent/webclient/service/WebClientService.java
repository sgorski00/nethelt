package pl.sgorski.nethelt.agent.webclient.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pl.sgorski.nethelt.agent.model.*;
import pl.sgorski.nethelt.agent.webclient.api.DeviceClient;
import pl.sgorski.nethelt.agent.webclient.api.MonitoringResultClient;
import pl.sgorski.nethelt.agent.webclient.api.NetworkConfigClient;

@Slf4j
@Service
@RequiredArgsConstructor
public final class WebClientService {

  private final DeviceClient deviceClient;
  private final NetworkConfigClient networkConfigClient;
  private final MonitoringResultClient monitoringResultClient;

  public void sendPingResults(Set<PingResult> results) {
    log.debug("Sending {} ping results to the server", results.size());
    if (!CollectionUtils.isEmpty(results)) {
      monitoringResultClient.sendPingResults(results);
    }
  }

  public void sendTelnetResults(Set<TelnetResult> results) {
    log.debug("Sending {} telnet results to the server", results.size());
    if (!CollectionUtils.isEmpty(results)) {
      monitoringResultClient.sendTelnetResults(results);
    }
  }

  public Set<NetworkConfig> fetchNetworkConfig() {
    log.debug("Fetching network configuration from the server");
    return networkConfigClient.getNetworkConfigs();
  }

  public Set<Device> fetchDevices() {
    log.debug("Fetching devices from the server");
    return deviceClient.getDevices();
  }
}
