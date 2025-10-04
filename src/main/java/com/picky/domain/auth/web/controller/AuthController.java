package com.picky.domain.auth.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.auth.TokenInfo;
import com.picky.domain.auth.service.AuthService;
import com.picky.domain.auth.web.dto.AuthResponseDTO;
import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "로그인 API",
            description = "사용자가 이메일과 비밀번호로 로그인합니다."
    )
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        String firebaseToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.login(firebaseToken));
    }

    @Operation(
            summary = "회원가입 API",
            description = "사용자가 이메일, 비밀번호 등의 정보로 회원가입합니다."
    )
    @PostMapping("/signup")
    public ApiResponse<AuthResponseDTO> signUp(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader, @RequestBody MemberSignUpRequestDTO memberSignUpRequestDTO) {
        String firebaseToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.signUp(firebaseToken, memberSignUpRequestDTO));
    }

    @Operation(
            summary = "토큰 재발급 API",
            description = "리프레쉬 토큰 정보로 액세스 토큰과 리프레쉬 토큰을 재발급합니다."
    )
    @PostMapping("/reissue")
    public ApiResponse<TokenInfo> reissue(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.reissue(refreshToken));
    }

    @Operation(
            summary = "로그아웃 API",
            description = "사용자가 로그아웃합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomerUserDetails customerUserDetails) {
        String email = customerUserDetails.getUsername();
        authService.logout(email);
        return ApiResponse.onSuccess(null);
    }

    private String extractToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
