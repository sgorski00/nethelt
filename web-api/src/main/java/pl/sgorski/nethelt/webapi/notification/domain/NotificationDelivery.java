package pl.sgorski.nethelt.webapi.notification.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

@Getter
@Entity
@Table(
    name = "notification_deliveries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "channel"}))
@ToString(exclude = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDelivery {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "notification_id", nullable = false)
  private Notification notification;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationDeliveryStatus status = NotificationDeliveryStatus.PENDING;

  @Column(nullable = false)
  private int attempts;

  @Nullable private String lastError;

  @Nullable private Instant nextAttemptAt;

  @Nullable private Instant lastAttemptAt;

  @Nullable private Instant deliveredAt;

  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  public NotificationDelivery(Notification notification, NotificationChannel channel) {
    this.notification = notification;
    this.channel = channel;
  }

  public void markSent() {
    var now = Instant.now();
    this.attempts++;
    this.status = NotificationDeliveryStatus.SENT;
    this.lastAttemptAt = now;
    this.deliveredAt = now;
    this.nextAttemptAt = null;
    this.lastError = null;
  }

  public void markSkipped(String reason) {
    this.attempts++;
    this.status = NotificationDeliveryStatus.SKIPPED;
    this.lastAttemptAt = Instant.now();
    this.nextAttemptAt = null;
    this.lastError = reason;
  }

  /**
   * Records a failed attempt. The next attempt is scheduled with exponential backoff ({@code
   * retryBackoff * 2^(attempts - 1)}) until {@code maxAttempts} is reached.
   */
  public void markFailed(String error, int maxAttempts, Duration retryBackoff) {
    var now = Instant.now();
    this.attempts++;
    this.lastAttemptAt = now;
    this.lastError = error;
    if (attempts >= maxAttempts) {
      this.status = NotificationDeliveryStatus.EXHAUSTED;
      this.nextAttemptAt = null;
    } else {
      this.status = NotificationDeliveryStatus.FAILED;
      this.nextAttemptAt = now.plus(retryBackoff.multipliedBy(1L << (attempts - 1)));
    }
  }
}
