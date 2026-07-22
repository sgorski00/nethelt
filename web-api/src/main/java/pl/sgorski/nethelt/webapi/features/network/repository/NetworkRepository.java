package pl.sgorski.nethelt.webapi.features.network.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.features.network.domain.Network;

public interface NetworkRepository extends JpaRepository<Network, Long> {}
