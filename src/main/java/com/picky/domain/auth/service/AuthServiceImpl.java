package com.picky.domain.auth.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.auth.JwtTokenProvider;
import com.picky.domain.auth.TokenInfo;
import com.picky.domain.auth.web.dto.AuthResponseDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseAuth firebaseAuth;

    @Override
    @Transactional
    public AuthResponseDTO loginOrSignUp(String firebaseToken) {
        FirebaseToken decodedToken;
        try {
            decodedToken = firebaseAuth.verifyIdToken(firebaseToken);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED, "유효하지 않은 Firebase 토큰입니다.");
        }

        String email = decodedToken.getEmail();
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        boolean isNewUser = optionalMember.isEmpty();

        Member member = optionalMember.orElseGet(() -> {

            String name = decodedToken.getName();
            if (name == null || name.isBlank()) {
                // 이메일에서 @ 앞부분을 이름으로 사용
                name = email.split("@")[0];
            }

            Member newMember = Member.builder()
                    .email(email)
                    .name(name)
                    .nickname("Picky" + email.split("@")[0])
                    .roles(Collections.singletonList("USER"))
                    .build();
            return memberRepository.save(newMember);
        });

        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

        TokenInfo tokenInfo = jwtTokenProvider.generateToken(authentication);

        return AuthResponseDTO.builder()
                .status(isNewUser ? AuthResponseDTO.AuthStatus.SIGN_UP : AuthResponseDTO.AuthStatus.LOGIN)
                .tokenInfo(tokenInfo)
                .build();
    }
}
