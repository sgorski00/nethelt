package pl.sgorski.nethelt.webapi.features.device.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;

public interface DeviceRepository
    extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
  Optional<Device> findByIdAndNetworkId(Long deviceId, Long networkId);

  Set<Device> findAllByNetworkId(Long networkId);
}
