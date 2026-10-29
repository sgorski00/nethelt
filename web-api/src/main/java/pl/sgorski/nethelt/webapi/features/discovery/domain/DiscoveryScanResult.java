package pl.sgorski.nethelt.webapi.features.discovery.domain;

import jakarta.persistence.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@Getter
@Entity
@Table(name = "discovery_scan_results")
@EqualsAndHashCode(exclude = "scan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscoveryScanResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "scan_id", nullable = false)
  private DiscoveryScan scan;

  @Column(nullable = false, columnDefinition = "inet")
  private InetAddress ipAddress;

  @Nullable private String hostname;

  @Column(nullable = false)
  private int responseTimeMs;

  DiscoveryScanResult(
      DiscoveryScan scan, Inet4Address ipAddress, @Nullable String hostname, int responseTimeMs) {
    this.scan = scan;
    this.ipAddress = ipAddress;
    this.hostname = hostname;
    this.responseTimeMs = responseTimeMs;
  }
}
