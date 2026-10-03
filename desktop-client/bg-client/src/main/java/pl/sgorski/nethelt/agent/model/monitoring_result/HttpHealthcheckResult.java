package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public final class HttpHealthcheckResult extends Result {

  private final Integer statusCode;

  public HttpHealthcheckResult(
      Long taskId, boolean success, String message, long responseTimeMs, Integer statusCode) {
    super(taskId, success, message, responseTimeMs);
    this.statusCode = statusCode;
  }
}
