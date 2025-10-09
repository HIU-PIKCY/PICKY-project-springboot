package com.picky.domain.notification.web.dto;

import com.picky.domain.notification.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;
    private String content;

    @Schema(description = "알림 타입 (NEW_ANSWER, NEW_REPLY, QUESTION_LIKE, VIEW_COUNT)")
    private NotificationType notificationType;

    @Schema(description = "알림 클릭 시 이동할 대상 ID (예: questionId)")
    private Long questionId; // 알림과 관련된 대상 ID (예: 질문 ID)

    private String profileImg; // 알림을 보낸 사람의 프로필 이미지 URL

    @Schema(description = "대댓글 알림의 경우, 부모 댓글의 ID")
    private Long parentId;

    @Schema(description = "알림 읽음 여부")
    private Boolean isRead;

    private LocalDateTime createdAt;
}