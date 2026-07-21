package pl.sgorski.nethelt.webapi.notification.domain;

import jakarta.persistence.*;
import java.util.EnumSet;
import java.util.Set;
import lombok.*;
import pl.sgorski.nethelt.webapi.features.user.domain.User;

@Getter
@Entity
@Table(name = "notification_preferences")
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = {"user", "enabledChannels"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreferences {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "channel", nullable = false)
  @Enumerated(EnumType.STRING)
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "notification_enabled_channels",
      joinColumns = @JoinColumn(name = "preference_id", nullable = false))
  private final Set<NotificationChannel> enabledChannels = EnumSet.allOf(NotificationChannel.class);

  public NotificationPreferences(User user) {
    this.user = user;
  }

  public boolean isChannelEnabled(NotificationChannel channel) {
    return enabledChannels.contains(channel);
  }

  public void enableChannel(NotificationChannel channel) {
    enabledChannels.add(channel);
  }

  public void disableChannel(NotificationChannel channel) {
    enabledChannels.remove(channel);
  }
}
