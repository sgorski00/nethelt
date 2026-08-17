package pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.HttpHealthcheckTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.PingTaskConfigurationRequest;
import pl.sgorski.nethelt.webapi.features.monitoring_task.dto.request.configuration.TelnetTaskConfigurationRequest;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PingTaskConfigurationRequest.class, name = "PING"),
  @JsonSubTypes.Type(value = TelnetTaskConfigurationRequest.class, name = "TELNET"),
  @JsonSubTypes.Type(
      value = HttpHealthcheckTaskConfigurationRequest.class,
      name = "HTTP_HEALTHCHECK")
})
public interface MonitoringTaskConfigurationRequest {}
