package pl.sgorski.nethelt.webapi.features.agent.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Getter
@Entity
@Table(name = "network_agents")
@EqualsAndHashCode(exclude = "network")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "network_id", nullable = false, unique = true)
  private Network network;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String hashedToken;

  @Column(nullable = false)
  private Instant tokenCreatedAt;

  @Nullable private Instant lastHeartbeatAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AgentStatus status = AgentStatus.ACTIVE;

  @Embedded private AgentConfiguration configuration = AgentConfiguration.defaults();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AlertState availabilityAlertState = AlertState.OK;

  public Agent(Network network, String name, String hashedToken) {
    this.network = network;
    this.name = name;
    this.hashedToken = hashedToken;
    this.tokenCreatedAt = Instant.now();
  }

  public void changeName(String name) {
    this.name = name;
  }

  public void changeToken(String hashedToken) {
    this.hashedToken = hashedToken;
    this.tokenCreatedAt = Instant.now();
  }

  /**
   * Records a heartbeat and resolves the availability alert.
   *
   * @return {@code true} when the alert was firing, so the agent is back online
   */
  public boolean heartbeat() {
    this.lastHeartbeatAt = Instant.now();
    if (availabilityAlertState == AlertState.FIRING) {
      this.availabilityAlertState = AlertState.OK;
      return true;
    }
    return false;
  }

  /**
   * Fires the availability alert when an active agent has not sent a heartbeat for longer than
   * {@code maxUnavailabilityTime}.
   *
   * @return {@code true} when the alert changed from {@code OK} to {@code FIRING}
   */
  public boolean fireAvailabilityAlertIfOffline(Instant now) {
    if (!isActive()
        || availabilityAlertState == AlertState.FIRING
        || connectionState(now) != AgentConnectionState.OFFLINE) {
      return false;
    }
    this.availabilityAlertState = AlertState.FIRING;
    return true;
  }

  public void deactivate() {
    this.status = AgentStatus.DISABLED;
  }

  public void activate() {
    this.status = AgentStatus.ACTIVE;
  }

  public boolean isActive() {
    return this.status == AgentStatus.ACTIVE;
  }

  public void changeConfiguration(AgentConfiguration configuration) {
    this.configuration = configuration;
  }

  public AgentConnectionState connectionState(Instant now) {
    if (lastHeartbeatAt == null) {
      return AgentConnectionState.NEVER_CONNECTED;
    }
    var deadline = lastHeartbeatAt.plus(configuration.getMaxUnavailabilityTime());
    return now.isAfter(deadline) ? AgentConnectionState.OFFLINE : AgentConnectionState.ONLINE;
  }
}
