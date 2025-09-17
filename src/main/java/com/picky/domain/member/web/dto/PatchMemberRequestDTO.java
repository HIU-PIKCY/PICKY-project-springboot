package com.picky.domain.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "내 프로필 수정 요청 DTO")
public class PatchMemberRequestDTO {

    @Schema(example = "피키", description = "닉네임", required = false)
    private String nickname;

    @Schema(example = "http://s3...", description = "프로필 이미지 URL", required = false)
    private String profileImg;
}
