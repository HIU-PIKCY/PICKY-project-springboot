package com.picky.domain.auth.web.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.service.AuthService;
import com.picky.domain.auth.web.dto.AuthResponseDTO;
import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> login(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        String firebaseToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.login(firebaseToken));
    }

    @PostMapping("/signup")
    public ApiResponse<AuthResponseDTO> signUp(@RequestHeader("Authorization") String authorizationHeader, @RequestBody MemberSignUpRequestDTO memberSignUpRequestDTO) {
        String firebaseToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.signUp(firebaseToken, memberSignUpRequestDTO));
    }

    private String extractToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
