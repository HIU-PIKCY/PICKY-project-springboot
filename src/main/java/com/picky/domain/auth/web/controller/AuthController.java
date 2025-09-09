package com.picky.domain.auth.web.controller;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picky.apiPayload.ApiResponse;
import com.picky.domain.auth.service.AuthService;
import com.picky.domain.auth.web.dto.AuthResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO> loginOrSignUp(@RequestHeader("Authorization") String authorizationHeader) {
        String firebaseToken = extractToken(authorizationHeader);
        return ApiResponse.onSuccess(authService.loginOrSignUp(firebaseToken));
    }

    private String extractToken(String header) {
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }
}
