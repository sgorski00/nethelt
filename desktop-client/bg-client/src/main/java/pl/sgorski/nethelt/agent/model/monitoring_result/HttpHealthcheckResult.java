package pl.sgorski.nethelt.agent.model.monitoring_result;

import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public final class HttpHealthcheckResult extends Result {

  private final @Nullable Integer statusCode;

  public HttpHealthcheckResult(
      Long taskId,
      boolean success,
      String message,
      @Nullable Long responseTimeMs,
      @Nullable Integer statusCode) {
    super(taskId, success, message, responseTimeMs);
    this.statusCode = statusCode;
  }
}
