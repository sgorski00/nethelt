package pl.sgorski.nethelt.webapi.features.device.domain;

import jakarta.persistence.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.exception.domain.device.DeviceValidationFailedException;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Entity
@Table(
    name = "devices",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"network_id", "name"}),
      @UniqueConstraint(columnNames = {"network_id", "ip_address"})
    })
@Getter
@EqualsAndHashCode(exclude = "network")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "network_id", nullable = false)
  private Network network;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, columnDefinition = "inet")
  private InetAddress ipAddress;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeviceType type;

  @Column(nullable = false)
  private boolean isEnabled = true;

  @CreationTimestamp
  @Column(nullable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private Instant updatedAt;

  public Device(Network network, String name, InetAddress ipAddress, DeviceType type) {
    validateIpv4(ipAddress);

    this.network = network;
    this.name = name;
    this.ipAddress = ipAddress;
    this.type = type;
  }

  public void update(
      @Nullable String name, @Nullable InetAddress ipAddress, @Nullable DeviceType type) {
    if (name != null) this.name = name;
    if (type != null) this.type = type;
    if (ipAddress != null) {
      validateIpv4(ipAddress);
      this.ipAddress = ipAddress;
    }
  }

  public void enable() {
    this.isEnabled = true;
  }

  public void disable() {
    this.isEnabled = false;
  }

  private static void validateIpv4(InetAddress ipAddress) {
    if (!(ipAddress instanceof Inet4Address)) {
      throw new DeviceValidationFailedException("Invalid IPv4 address");
    }
  }
}
