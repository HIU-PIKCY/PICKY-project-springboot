package com.picky.domain.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "닉네임 중복 검사 응답 DTO")
public class NicknameCheckResponseDTO {
    @Schema(description = "검사한 닉네임", example = "피키")
    private String nickname;

    @Schema(description = "사용 가능 여부", example = "true")
    private boolean isAvailable;

    @Schema(description = "결과 메시지", example = "사용 가능한 닉네임입니다.")
    private String message;
}
