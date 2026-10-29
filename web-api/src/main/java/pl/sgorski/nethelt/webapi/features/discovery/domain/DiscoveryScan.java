package pl.sgorski.nethelt.webapi.features.discovery.domain;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Getter
@Entity
@Table(name = "discovery_scans")
@EqualsAndHashCode(exclude = "network")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscoveryScan {

  public static final long MAX_RANGE_SIZE = 256;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "network_id", nullable = false, unique = true)
  private Network network;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiscoveryScanStatus status;

  @Nullable private Long rangeStart;

  @Nullable private Long rangeEnd;

  @Column(nullable = false)
  private Instant requestedAt;

  @Nullable private Instant startedAt;

  @Nullable private Instant finishedAt;

  @Nullable private String failureReason;

  public DiscoveryScan(Network network, @Nullable Ipv4Range range, Instant now) {
    this.network = network;
    reset(range, now);
  }

  public void restart(@Nullable Ipv4Range range, Instant now) {
    reset(range, now);
  }

  public boolean isInProgress() {
    return status == DiscoveryScanStatus.PENDING || status == DiscoveryScanStatus.RUNNING;
  }

  /**
   * @return {@code true} when the scan changed to {@code TIMED_OUT}
   */
  public boolean timeOutIfExpired(Instant now, Duration timeout) {
    if (!isInProgress() || !now.isAfter(requestedAt.plus(timeout))) {
      return false;
    }
    this.status = DiscoveryScanStatus.TIMED_OUT;
    this.finishedAt = now;
    return true;
  }

  public @Nullable Ipv4Range getRange() {
    return rangeStart == null || rangeEnd == null ? null : new Ipv4Range(rangeStart, rangeEnd);
  }

  private void reset(@Nullable Ipv4Range range, Instant now) {
    this.status = DiscoveryScanStatus.PENDING;
    this.rangeStart = range == null ? null : range.start();
    this.rangeEnd = range == null ? null : range.end();
    this.requestedAt = now;
    this.startedAt = null;
    this.finishedAt = null;
    this.failureReason = null;
  }
}
