package pl.sgorski.nethelt.webapi.notification.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDelivery;
import pl.sgorski.nethelt.webapi.notification.domain.NotificationDeliveryStatus;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {

  // notification and its user are fetched eagerly, because senders read them outside a transaction
  @Query(
"""
        SELECT d FROM NotificationDelivery d
        JOIN FETCH d.notification n
        JOIN FETCH n.user
        WHERE d.status = :status AND d.nextAttemptAt <= :now
        ORDER BY d.nextAttemptAt
""")
  List<NotificationDelivery> findDue(
      @Param("status") NotificationDeliveryStatus status,
      @Param("now") Instant now,
      Pageable pageable);

  @Modifying
  @Query("DELETE FROM NotificationDelivery d WHERE d.status IN :statuses AND d.updatedAt < :before")
  int deleteByStatusInAndUpdatedAtBefore(
      @Param("statuses") Collection<NotificationDeliveryStatus> statuses,
      @Param("before") Instant before);
}
