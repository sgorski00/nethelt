package pl.sgorski.nethelt.webapi.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Entity
@Table(name = "notifications")
@ToString(exclude = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Getter
  @Column(nullable = false)
  private String title;

  @Getter
  @Column(nullable = false)
  private String content;

  @Nullable @Getter private Instant readAt;

  @Getter @CreationTimestamp private Instant createdAt;

  public Notification(User user, String title, String content) {
    this.user = user;
    this.title = title;
    this.content = content;
  }

  public void markAsRead() {
    if (!this.isRead()) {
      this.readAt = Instant.now();
    }
  }

  public boolean isRead() {
    return this.readAt != null;
  }
}
