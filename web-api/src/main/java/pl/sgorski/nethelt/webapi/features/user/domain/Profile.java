package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "profiles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Nullable
  @Column(nullable = false, unique = true)
  private String username;

  @Nullable
  @Column(nullable = false)
  private String firstName;

  @Nullable
  @Column(nullable = false)
  private String lastName;

  @Nullable
  @Column(nullable = false)
  private LocalDate birthDate;

  @Nullable
  @Column(nullable = false)
  private String bio;

  @CreationTimestamp private Instant createdAt;

  @UpdateTimestamp private Instant updatedAt;

  public Profile(
      String username, String firstName, String lastName, LocalDate birthDate, String bio) {
    if (username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    this.username = username;
    this.updatePersonalInformation(firstName, lastName, birthDate, bio);
  }

  public void updatePersonalInformation(String firstName, String lastName, LocalDate birthDate, String bio) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = birthDate;
    this.bio = bio;
  }

  void setUser(User user) {
    this.user = user;
    if (user.getProfile() != this) {
      user.setProfile(this);
    }
  }
}
