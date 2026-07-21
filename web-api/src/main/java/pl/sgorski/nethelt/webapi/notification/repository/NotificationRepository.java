package pl.sgorski.nethelt.webapi.notification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.sgorski.nethelt.webapi.notification.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Query(
      "SELECT n FROM Notification n WHERE n.user.id = :userId AND (:showRead = true OR n.readAt IS NULL)")
  Page<Notification> findByUserId(
      @Param("userId") Long userId, @Param("showRead") boolean showRead, Pageable pageable);

  Long countByUserIdAndReadAtIsNull(Long userId);

  @Query(
      "SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n WHERE n.id = :id AND n.user.id = :userId")
  boolean existsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
