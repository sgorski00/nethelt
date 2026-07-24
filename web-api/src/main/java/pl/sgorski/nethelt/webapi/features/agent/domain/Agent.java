package pl.sgorski.nethelt.webapi.features.agent.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Entity
@Table(name = "network_agents")
@EqualsAndHashCode(exclude = "network")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Agent {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "network_id", nullable = false, unique = true)
  private Network network;

  @Getter
  @Column(nullable = false)
  private String name;

  // todo:
  // client should connect everyday with token and get generated jwt to authenticate every request
  // in jwt there should be agent context including:
  // - agent id
  // - network id
  @Getter
  @Column(nullable = false, unique = true)
  private String hashedToken;

  @Getter
  @Column(nullable = false)
  private Instant tokenCreatedAt;

  @Getter @Nullable private Instant lastHeartbeatAt;

  @Getter
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
}
