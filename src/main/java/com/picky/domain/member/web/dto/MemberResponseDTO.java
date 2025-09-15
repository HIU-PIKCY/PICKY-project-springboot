package com.picky.domain.member.web.dto;

import com.picky.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 프로필 정보 응답 DTO")
public class MemberResponseDTO {

        @Schema(example = "1", description = "유저 고유 ID")
        private Long id;

        @Schema(example = "picky@example.com", description = "이메일")
        private String email;

        @Schema(example = "키피럽", description = "이름")
        private String name;

        @Schema(example = "피키", description = "닉네임")
        private String nickname;

        @Schema(example = "http://s3...", description = "프로필 이미지 URL")
        private String profileImg;

        public static MemberResponseDTO fromEntity(Member member) {
            return MemberResponseDTO.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .nickname(member.getNickname())
                    .profileImg(member.getProfileImg())
                    .build();
        }
}