package com.picky.domain.member.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "사용자")
public class MemberController {

    @Operation(summary = "내 정보 조회 API", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/profile")
    public ApiResponse<MemberResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        MemberResponseDTO memberResponseDTO = MemberResponseDTO.fromEntity(currentMember);
        return ApiResponse.onSuccess(memberResponseDTO);
    }
}