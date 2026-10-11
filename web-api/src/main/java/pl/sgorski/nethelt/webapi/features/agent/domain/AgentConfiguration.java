package pl.sgorski.nethelt.webapi.features.agent.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.sgorski.nethelt.webapi.exception.domain.agent.AgentValidationFailedException;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AgentConfiguration {

  public static final Duration DEFAULT_MAX_UNAVAILABILITY_TIME = Duration.ofMinutes(5);

  @Column(nullable = false)
  @JdbcTypeCode(SqlTypes.INTERVAL_SECOND)
  private Duration maxUnavailabilityTime;

  public AgentConfiguration(Duration maxUnavailabilityTime) {
    if (!maxUnavailabilityTime.isPositive()) {
      throw new AgentValidationFailedException("max unavailability time must be positive");
    }
    this.maxUnavailabilityTime = maxUnavailabilityTime;
  }

  public static AgentConfiguration defaults() {
    return new AgentConfiguration(DEFAULT_MAX_UNAVAILABILITY_TIME);
  }
}
