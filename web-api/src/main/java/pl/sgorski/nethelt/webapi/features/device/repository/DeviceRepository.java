package pl.sgorski.nethelt.webapi.features.device.repository;

import jakarta.annotation.Nullable;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;
import pl.sgorski.nethelt.webapi.features.device.domain.DeviceType;

public interface DeviceRepository extends JpaRepository<Device, Long> {
  Set<Device> findAllByNetworkId(Long networkId);

  @Query(
"""
        SELECT d
        FROM Device d
        WHERE d.network.id = :networkId AND (:type IS NULL OR d.type = :type)
""")
  Page<Device> findAllByNetworkIdAndType(
      @Param("networkId") Long networkId,
      @Nullable @Param("type") DeviceType type,
      Pageable pageable);
}
