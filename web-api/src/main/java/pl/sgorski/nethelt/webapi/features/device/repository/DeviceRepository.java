package pl.sgorski.nethelt.webapi.features.device.repository;

import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
  Set<Device> findAllByNetworkId(Long networkId);
}
