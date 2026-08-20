package pl.sgorski.nethelt.agent.executor;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.sgorski.nethelt.agent.service.PingOperation;
import pl.sgorski.nethelt.agent.service.TelnetOperation;
import pl.sgorski.nethelt.model.Device;
import pl.sgorski.nethelt.model.PingResult;
import pl.sgorski.nethelt.model.TelnetResult;

@Component
@RequiredArgsConstructor
public class MonitoringExecutor {

  private final PingOperation ping;
  private final TelnetOperation telnet;

  public Set<PingResult> getPingResults(Set<Device> devices) {
    var futures =
        devices.stream()
            .map(device -> CompletableFuture.supplyAsync(() -> ping.execute(device)))
            .collect(Collectors.toSet());
    return waitForAll(futures);
  }

  public Set<TelnetResult> getTelnetResults(Set<Device> devices) {
    var futures =
        devices.stream()
            .filter(device -> Objects.nonNull(device.getPort()))
            .map(device -> CompletableFuture.supplyAsync(() -> telnet.execute(device)))
            .collect(Collectors.toSet());
    return waitForAll(futures);
  }

  private static <T> Set<T> waitForAll(Set<CompletableFuture<T>> futures) {
    return futures.stream().map(CompletableFuture::join).collect(Collectors.toSet());
  }
}
