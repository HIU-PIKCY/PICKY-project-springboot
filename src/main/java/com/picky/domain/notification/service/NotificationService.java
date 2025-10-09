package com.picky.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.member.entity.Member;
import com.picky.domain.notification.entity.Notification;
import com.picky.domain.notification.repository.NotificationRepository;
import com.picky.domain.notification.web.dto.NotificationResponseDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationRepository notificationRepository;

    public void sendNotification(
        String title,
        String body,
        String profileImg,
        Member targetMember,
        Map<String, String> data
    ) {
        // 대상 사용자의 FCM 토큰이 없는 경우 알림을 보내지 않음
        if (targetMember.getFcmToken() == null || targetMember.getFcmToken().isEmpty()) {
            log.warn("FCM Token is missing for member: {}", targetMember.getNickname());
            return;
        }

        // 앱이 백그라운드일 때 표시될 알림 내용
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
                                                .setTitle(title)
                                                .setBody(body)
                                                .setImage(profileImg)
                                                .build();

        Message message = Message.builder()
                                 .setToken(targetMember.getFcmToken())
                                 .setNotification(notification)
                                 .putAllData(data) // 프론트에서 활용할 데이터
                                 .build();

        try {
            // FCM에 메시지 전송 요청
            firebaseMessaging.send(message);
            log.info("Successfully sent notification to member: {}", targetMember.getNickname());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification to member: {}", targetMember.getNickname(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getNotifications(Long receiverId) {

        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);

        return notifications.stream()
                            .map(notification -> {
                                String profileImg = (notification.getSender() != null) ? notification.getSender().getProfileImg() : null;

                                return NotificationResponseDTO.builder()
                                                              .id(notification.getId())
                                                              .content(notification.getContent())
                                                              .notificationType(notification.getNotificationType())
                                                              .questionId(notification.getQuestionId())
                                                              .parentId(notification.getParentId())
                                                              .profileImg(profileImg)
                                                              .isRead(notification.getIsRead())
                                                              .createdAt(notification.getCreatedAt())
                                                              .build();
                            })
                            .collect(Collectors.toList());
    }

    @Transactional
    public void readNotification(Long notificationId, Long receiverId) {
        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, receiverId)
                                                        .orElseThrow(() -> new GeneralException(ErrorStatus.NOTIFICATION_NOT_FOUND));

        notification.read();
    }
}
