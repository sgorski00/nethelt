package pl.sgorski.nethelt.agent.webclient.api.web;

import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import pl.sgorski.nethelt.agent.model.PingResult;
import pl.sgorski.nethelt.agent.model.TelnetResult;

@HttpExchange(url = "/monitoring/results")
public interface MonitoringResultClient {
  @PostMapping("/ping")
  void sendPingResults(@RequestBody Set<PingResult> results);

  @PostMapping("/telnet")
  void sendTelnetResults(@RequestBody Set<TelnetResult> results);
}
