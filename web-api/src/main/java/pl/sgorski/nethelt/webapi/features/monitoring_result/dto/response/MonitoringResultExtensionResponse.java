package pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.HttpHealthcheckResultExtensionResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.PingResultExtensionResponse;
import pl.sgorski.nethelt.webapi.features.monitoring_result.dto.response.extension.TelnetResultExtensionResponse;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PingResultExtensionResponse.class, name = "PING"),
  @JsonSubTypes.Type(value = TelnetResultExtensionResponse.class, name = "TELNET"),
  @JsonSubTypes.Type(
      value = HttpHealthcheckResultExtensionResponse.class,
      name = "HTTP_HEALTHCHECK")
})
public interface MonitoringResultExtensionResponse {}
