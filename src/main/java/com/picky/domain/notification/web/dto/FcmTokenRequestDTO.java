package com.picky.domain.notification.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmTokenRequestDTO {

    @Schema(description = "알림을 받을 사람의 토큰")
    private String fcmToken;
}
