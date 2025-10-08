package com.picky.domain.notification.repository;

import com.picky.domain.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<Notification> findByIdAndMemberId(Long notificationId, Long memberId);
}
