package pl.sgorski.nethelt.webapi.features.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@NoArgsConstructor
@Getter
@Setter
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private String bio;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    void setUser(User user) {
        this.user = user;
        if(user.getProfile() != this) {
            user.setProfile(this);
        }
    }
}
