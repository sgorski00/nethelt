package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.HttpHealthcheckResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.PingResultExtensionRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.request.extension.TelnetResultExtensionRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PingResultExtensionRequest.class, name = "PING"),
  @JsonSubTypes.Type(value = TelnetResultExtensionRequest.class, name = "TELNET"),
  @JsonSubTypes.Type(value = HttpHealthcheckResultExtensionRequest.class, name = "HTTP_HEALTHCHECK")
})
public interface MonitoringResultExtensionRequest {}
