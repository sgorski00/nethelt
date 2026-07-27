package pl.sgorski.nethelt.webapi.features.network.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.agent.domain.Agent;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Entity
@Table(name = "networks")
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Network {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @Column(name = "name", nullable = false)
  private String name;

  @Getter @Nullable private String description;

  @Getter
  @Nullable
  @OneToOne(mappedBy = "network", cascade = CascadeType.ALL, orphanRemoval = true)
  private Agent agent;

  @Getter @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  @Nullable private Instant deletedAt;

  public Network(User user, String name, @Nullable String description) {
    this.user = user;
    this.name = name;
    this.description = description;
  }

  public void delete() {
    this.deletedAt = Instant.now();
    this.agent = null;
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void update(String name, @Nullable String description) {
    this.name = name;
    this.description = description;
  }
}
