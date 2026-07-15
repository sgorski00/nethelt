package pl.sgorski.nethelt.webapi.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {}
