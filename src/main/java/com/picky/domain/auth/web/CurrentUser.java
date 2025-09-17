package com.picky.domain.auth.web;

import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.member.entity.Member;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    /**
     * 현재 로그인한 Member 객체를 반환합니다.
     * 로그인 상태가 아니라면 예외를 던집니다.
     */
    public static Member get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomerUserDetails)) {
            throw new IllegalStateException("로그인된 유저가 없습니다.");
        }

        return ((CustomerUserDetails) authentication.getPrincipal()).getMember();
    }

    /**
     * 현재 로그인한 회원 ID 반환
     */
    public static Long getId() {
        return get().getId();
    }

    /**
     * 현재 로그인한 회원 이메일 반환
     */
    public static String getEmail() {
        return get().getEmail();
    }
}
