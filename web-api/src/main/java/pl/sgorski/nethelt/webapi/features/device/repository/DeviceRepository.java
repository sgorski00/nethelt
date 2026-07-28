package pl.sgorski.nethelt.webapi.features.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.device.domain.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {}
