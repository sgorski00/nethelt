package pl.sgorski.nethelt.agent.model.monitoring_task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HttpScheme {
  HTTP("http"),
  HTTPS("https");

  private final String value;
}
