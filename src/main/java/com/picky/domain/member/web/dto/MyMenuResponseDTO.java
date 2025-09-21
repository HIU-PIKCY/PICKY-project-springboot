package com.picky.domain.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "마이메뉴 조회 응답 DTO")
public class MyMenuResponseDTO {

    private MemberResponseDTO user;
    private MemberStatusDTO stats;
}
