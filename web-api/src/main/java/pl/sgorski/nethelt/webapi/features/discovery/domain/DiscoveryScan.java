package pl.sgorski.nethelt.webapi.features.discovery.domain;

import jakarta.persistence.*;
import java.net.Inet4Address;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanNotAllowedException;
import pl.sgorski.nethelt.webapi.exception.domain.discovery.DiscoveryScanValidationFailedException;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

@Getter
@Entity
@Table(name = "discovery_scans")
@EqualsAndHashCode(exclude = {"network", "results"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscoveryScan {

  public static final long MAX_RANGE_SIZE = 256;
  private static final int MAX_FAILURE_REASON_LENGTH = 255;

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

  @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("ipAddress")
  private List<DiscoveryScanResult> results = new ArrayList<>();

  public DiscoveryScan(Network network, @Nullable Ipv4Range range) {
    this.network = network;
    reset(range);
  }

  public void restart(@Nullable Ipv4Range range) {
    reset(range);
  }

  public boolean isInProgress() {
    return status == DiscoveryScanStatus.PENDING || status == DiscoveryScanStatus.RUNNING;
  }

  public boolean isExpired(Instant now, Duration timeout) {
    return isInProgress() && now.isAfter(requestedAt.plus(timeout));
  }

  /**
   * @return {@code true} when the scan changed to {@code TIMED_OUT}
   */
  public boolean timeOutIfExpired(Instant now, Duration timeout) {
    if (!isExpired(now, timeout)) {
      return false;
    }
    this.status = DiscoveryScanStatus.TIMED_OUT;
    this.finishedAt = now;
    return true;
  }

  public void start(Ipv4Range range) {
    requireStatus(DiscoveryScanStatus.PENDING);
    setRange(range);
    this.status = DiscoveryScanStatus.RUNNING;
    this.startedAt = Instant.now();
  }

  public void addResult(Inet4Address ipAddress, @Nullable String hostname, int responseTimeMs) {
    requireStatus(DiscoveryScanStatus.RUNNING);
    var range = getRange();
    if (range == null || !range.contains(Ipv4Range.toNumber(ipAddress))) {
      throw new DiscoveryScanValidationFailedException(
          ipAddress.getHostAddress() + " is outside of the scanned range");
    }
    if (results.stream().anyMatch(result -> result.getIpAddress().equals(ipAddress))) {
      throw new DiscoveryScanValidationFailedException(
          ipAddress.getHostAddress() + " is reported more than once");
    }
    results.add(new DiscoveryScanResult(this, ipAddress, hostname, responseTimeMs));
  }

  public void complete() {
    requireStatus(DiscoveryScanStatus.RUNNING);
    this.status = DiscoveryScanStatus.COMPLETED;
    this.finishedAt = Instant.now();
  }

  public void fail(String reason) {
    if (!isInProgress()) {
      throw new DiscoveryScanNotAllowedException("The discovery scan is " + status);
    }
    this.status = DiscoveryScanStatus.FAILED;
    this.failureReason =
        reason.length() > MAX_FAILURE_REASON_LENGTH
            ? reason.substring(0, MAX_FAILURE_REASON_LENGTH)
            : reason;
    this.finishedAt = Instant.now();
  }

  public @Nullable Ipv4Range getRange() {
    return rangeStart == null || rangeEnd == null ? null : new Ipv4Range(rangeStart, rangeEnd);
  }

  private void reset(@Nullable Ipv4Range range) {
    if (range != null) {
      setRange(range);
    } else {
      this.rangeStart = null;
      this.rangeEnd = null;
    }
    this.status = DiscoveryScanStatus.PENDING;
    this.requestedAt = Instant.now();
    this.startedAt = null;
    this.finishedAt = null;
    this.failureReason = null;
    this.results.clear();
  }

  private void setRange(Ipv4Range range) {
    if (range.size() > MAX_RANGE_SIZE) {
      throw new DiscoveryScanValidationFailedException(
          "the IP range can have at most " + MAX_RANGE_SIZE + " addresses (/24)");
    }
    this.rangeStart = range.start();
    this.rangeEnd = range.end();
  }

  private void requireStatus(DiscoveryScanStatus expected) {
    if (status != expected) {
      throw new DiscoveryScanNotAllowedException(
          "The discovery scan is " + status + ", expected " + expected);
    }
  }
}
