package pl.sgorski.nethelt.agent.webclient.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PingResultExtensionRequest.class, name = "PING"),
  @JsonSubTypes.Type(value = TelnetResultExtensionRequest.class, name = "TELNET"),
  @JsonSubTypes.Type(value = HttpHealthcheckResultExtensionRequest.class, name = "HTTP_HEALTHCHECK")
})
public sealed interface MonitoringResultExtensionRequest
    permits PingResultExtensionRequest,
        TelnetResultExtensionRequest,
        HttpHealthcheckResultExtensionRequest {}
