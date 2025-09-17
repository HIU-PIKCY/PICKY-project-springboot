package com.picky.domain.member.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.service.MemberService;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import com.picky.domain.member.web.dto.PatchMemberRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "사용자")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "내 정보 조회 API", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/profile")
    public ApiResponse<MemberResponseDTO> getMyProfile(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails
    ) {
        Member currentMember = customerUserDetails.getMember();
        MemberResponseDTO memberResponseDTO = MemberResponseDTO.fromEntity(currentMember);
        return ApiResponse.onSuccess(memberResponseDTO);
    }

    @Operation(summary = "내 정보 수정 API", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/profile")
    public ApiResponse<MemberResponseDTO> patchMember(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails, PatchMemberRequestDTO request
    ) {
        Member currentMember = customerUserDetails.getMember();
        MemberResponseDTO memberResponseDTO = memberService.patchMember(currentMember, request);
        return ApiResponse.onSuccess(memberResponseDTO);
    }
}