package pl.sgorski.nethelt.agent.webclient.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PingTaskConfigurationResponse.class, name = "PING"),
  @JsonSubTypes.Type(value = TelnetTaskConfigurationResponse.class, name = "TELNET"),
  @JsonSubTypes.Type(
      value = HttpHealthcheckTaskConfigurationResponse.class,
      name = "HTTP_HEALTHCHECK")
})
public sealed interface MonitoringTaskConfigurationResponse
    permits PingTaskConfigurationResponse,
        TelnetTaskConfigurationResponse,
        HttpHealthcheckTaskConfigurationResponse {}
