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

  public void heartbeat() {
    this.lastHeartbeatAt = Instant.now();
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
}
