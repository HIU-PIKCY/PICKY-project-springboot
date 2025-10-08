package com.picky.domain.notification.entity;

import com.picky.domain.member.entity.Member;
import com.picky.domain.notification.entity.enums.NotificationType;
import com.picky.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_notification_member"))
    private Member member; // 알림을 받은 사용자

    @Column(nullable = false)
    private String content; // 알림 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    @Column(nullable = false)
    private Long questionId; // 알림 클릭 시 이동할 대상의 ID (questionId)

    @Column(nullable = true)
    private Long parentId; // 대댓글이면 부모 댓글 ID

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false; // 읽음 여부

    public void read() {
        this.isRead = true;
    }
}
