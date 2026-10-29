package pl.sgorski.nethelt.agent.webclient.api.web;

import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanAckRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanFailureRequest;
import pl.sgorski.nethelt.agent.webclient.dto.request.DiscoveryScanResultsRequest;
import pl.sgorski.nethelt.agent.webclient.dto.response.DiscoveryScanResponse;

@Validated
@HttpExchange(url = "/client/discovery-scan")
public interface DiscoveryScanClient {

  @GetExchange("/pending")
  Optional<DiscoveryScanResponse> fetchPendingScan();

  @PostExchange("/ack")
  void acknowledgeScan(@Valid @RequestBody DiscoveryScanAckRequest request);

  @PostExchange("/results")
  void sendResults(@Valid @RequestBody DiscoveryScanResultsRequest request);

  @PostExchange("/failure")
  void reportFailure(@Valid @RequestBody DiscoveryScanFailureRequest request);
}
