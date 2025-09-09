package com.picky.domain.auth.web.dto;

import com.picky.domain.auth.TokenInfo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDTO {
    private AuthStatus status; // 신규 가입(SIGN_UP)인지 기존 로그인(LOGIN)인지 알려주는 상태
    private TokenInfo tokenInfo;

    public enum AuthStatus {
        SIGN_UP, LOGIN
    }
}
