package com.deiconnect.notification.repository;

import com.deiconnect.notification.entity.Notification;
import com.deiconnect.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_Id(Long userId, Pageable pageable);

    Page<Notification> findByUser_IdAndStatus(Long userId, NotificationStatus status, Pageable pageable);

    long countByUser_IdAndStatus(Long userId, NotificationStatus status);

    boolean existsByIdAndEmployeeIdAndUser_IdAndStatus(Long id,
                                                      String employeeId,
                                                      Long userId,
                                                      NotificationStatus status);

    @Modifying
    @Query("update Notification n set n.status = com.deiconnect.notification.enums.NotificationStatus.READ "
            + "where n.user.id = :userId "
            + "and n.status = com.deiconnect.notification.enums.NotificationStatus.UNREAD")
    int markAllRead(@Param("userId") Long userId);
}
