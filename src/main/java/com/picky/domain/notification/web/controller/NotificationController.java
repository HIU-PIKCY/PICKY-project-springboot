package com.picky.domain.notification.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.service.MemberService;
import com.picky.domain.notification.service.NotificationService;
import com.picky.domain.notification.web.dto.FcmTokenRequestDTO;
import com.picky.domain.notification.web.dto.NotificationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "알림")
public class NotificationController {

    private final MemberService memberService;
    private final NotificationService notificationService;

    @Operation(summary = "FCM 토큰 저장 API", description = "사용자의 FCM 토큰을 저장합니다.")
    @PostMapping("/fcm-token")
    public ApiResponse<Void> updateFcmToken(@AuthenticationPrincipal CustomerUserDetails customerUserDetails,
                                            @Valid @RequestBody FcmTokenRequestDTO request) {
        Long memberId = customerUserDetails.getMember().getId();
        String fcmToken = request.getFcmToken();
        memberService.updateFcmToken(memberId, fcmToken);
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<NotificationResponseDTO>> getNotifications(@AuthenticationPrincipal CustomerUserDetails customerUserDetails) {
        Long memberId = customerUserDetails.getMember().getId();
        List<NotificationResponseDTO> notifications = notificationService.getNotifications(memberId);
        return ApiResponse.onSuccess(notifications);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> readNotification(@AuthenticationPrincipal CustomerUserDetails customerUserDetails,
                                        @PathVariable Long notificationId) {
        Long memberId = customerUserDetails.getMember().getId();
        notificationService.readNotification(notificationId, memberId);
        return ApiResponse.onSuccess(null);
    }
}
