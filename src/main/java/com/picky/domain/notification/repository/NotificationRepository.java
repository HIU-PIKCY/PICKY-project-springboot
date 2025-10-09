package com.picky.domain.notification.repository;

import com.picky.domain.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n LEFT JOIN FETCH n.sender WHERE n.receiver.id = :receiverId ORDER BY n.createdAt DESC")
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId);

    Optional<Notification> findByIdAndReceiverId(Long notificationId, Long receiverId);
}
