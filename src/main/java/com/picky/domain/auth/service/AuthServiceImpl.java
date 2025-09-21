package com.picky.domain.auth.service;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;
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
import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final FirebaseAuth firebaseAuth;

    @Override
    @Transactional
    public AuthResponseDTO login(String firebaseToken) {

        FirebaseToken decodedToken = verifyFirebaseToken(firebaseToken);
        String email = decodedToken.getEmail();

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND, "가입되지 않은 회원입니다.")
        );

        TokenInfo tokenInfo = generateToken(member);

        return AuthResponseDTO.builder()
                .status(AuthResponseDTO.AuthStatus.LOGIN)
                .tokenInfo(tokenInfo)
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDTO signUp(String firebaseToken, MemberSignUpRequestDTO memberSignUpRequestDTO) {
        FirebaseToken decodedToken = verifyFirebaseToken(firebaseToken);
        String email = decodedToken.getEmail();
        log.info("decoded firebase email: {}", email);

        memberRepository.findByEmail(email).ifPresent(member -> {
            throw new GeneralException(ErrorStatus._BAD_REQUEST, "이미 가입된 회원입니다.");
        });
        log.info(email);

        Member newMember = Member.builder()
                .email(email)
                .name(memberSignUpRequestDTO.getName())
                .nickname(memberSignUpRequestDTO.getNickname())
                .roles(Collections.singletonList("USER"))
                .build();
        memberRepository.save(newMember);

        TokenInfo tokenInfo = generateToken(newMember);

        return AuthResponseDTO.builder()
                .status(AuthResponseDTO.AuthStatus.SIGN_UP)
                .tokenInfo(tokenInfo)
                .build();
    }

    private FirebaseToken verifyFirebaseToken(String firebaseToken) {
        try {
            return firebaseAuth.verifyIdToken(firebaseToken);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED, "유효하지 않은 Firebase 토큰입니다.");
        }
    }

    private TokenInfo generateToken(Member member) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        return jwtTokenProvider.generateToken(authentication);

    }
}