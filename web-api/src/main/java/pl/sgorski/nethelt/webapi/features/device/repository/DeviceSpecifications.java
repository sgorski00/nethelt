package pl.sgorski.nethelt.webapi.features.device.repository;

import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;
import pl.sgorski.nethelt.webapi.features.device.domain.Ipv4Range;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeviceSpecifications {

  public static Specification<Device> inNetwork(Long networkId) {
    return (root, _, cb) -> cb.equal(root.get("network").get("id"), networkId);
  }

  public static Specification<Device> hasType(DeviceType type) {
    return (root, _, cb) -> cb.equal(root.get("type"), type);
  }

  public static Specification<Device> nameContains(String name) {
    var pattern = "%" + escapeLike(name.strip().toLowerCase(Locale.ROOT)) + "%";
    return (root, _, cb) -> cb.like(cb.lower(root.get("name")), pattern, '\\');
  }

  public static Specification<Device> ipInAnyRange(List<Ipv4Range> ranges) {
    return (root, _, cb) ->
        cb.or(
            ranges.stream()
                .map(range -> cb.between(root.get("ipNumeric"), range.start(), range.end()))
                .toArray(Predicate[]::new));
  }

  private static String escapeLike(String value) {
    return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }
}
