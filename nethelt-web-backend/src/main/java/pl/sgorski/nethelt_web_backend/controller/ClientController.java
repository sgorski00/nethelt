package pl.sgorski.nethelt_web_backend.controller;

import java.util.List;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sgorski.nethelt.core.model.Device;
import pl.sgorski.nethelt.core.model.NetworkConfig;
import pl.sgorski.nethelt.core.model.Operation;
import pl.sgorski.nethelt.core.model.PingResult;
import pl.sgorski.nethelt.core.model.TelnetResult;

@RestController
@RequestMapping("/api")
public class ClientController {

  @PostMapping("/ping")
  public ResponseEntity<?> receivePingResults(@RequestBody Set<PingResult> body) {
    System.out.println("Received ping results: " + body);
    return ResponseEntity.ok(body);
  }

  @PostMapping("/telnet")
  public ResponseEntity<?> receiveTelnetResults(@RequestBody Set<TelnetResult> body) {
    System.out.println("Received telnet results: " + body);
    return ResponseEntity.ok(body);
  }

  @GetMapping("/devices")
  public ResponseEntity<?> getDevices() {
    Set<Device> devices = Set.of(
      new Device("Server2", "150.158.50.2"),
      new Device("Server3", "150.158.50.3"),
      new Device("Server8 - etatyczytnik", "150.158.50.8", 80)
//      ,new Device("notexistingip", "166.166.166.166"),
//      new Device("closedport", "150.158.50.8", 65535)
    );
    return ResponseEntity.ok(devices);
  }

  @GetMapping("/config/network")
  public ResponseEntity<?> getNetworkConfig() {
    List<NetworkConfig> config = List.of(
      new NetworkConfig(Operation.PING, true, 30),
      new NetworkConfig(Operation.TELNET, true, 45)
    );
    return ResponseEntity.ok(config);
  }
}
