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
import com.picky.domain.auth.entity.RefreshToken;
import com.picky.domain.auth.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public AuthResponseDTO login(String firebaseToken) {

        FirebaseToken decodedToken = verifyFirebaseToken(firebaseToken);
        String email = decodedToken.getEmail();

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND, "가입되지 않은 회원입니다.")
        );

        TokenInfo tokenInfo = generateToken(member);

        // 리프레쉬 토큰을 DB에 저장
        refreshTokenRepository.findByEmail(email).ifPresentOrElse(refreshToken -> {
//            refreshTokenRepository.delete(refreshToken); // 이게 꼭 필요할까?
            refreshToken.updateToken(tokenInfo.getRefreshToken());
            refreshTokenRepository.save(refreshToken);
        },
                () -> refreshTokenRepository.save(new RefreshToken(email, tokenInfo.getRefreshToken()))
        );

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

        // 리프레쉬 토큰을 DB에 저장
        refreshTokenRepository.save(new RefreshToken(email, tokenInfo.getRefreshToken()));

        return AuthResponseDTO.builder()
                .status(AuthResponseDTO.AuthStatus.SIGN_UP)
                .tokenInfo(tokenInfo)
                .build();
    }

    @Override
    @Transactional
    public TokenInfo reissue(String refreshToken) {

        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED, "유효하지 않은 refresh token입니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String email = authentication.getName();

        RefreshToken storedRefreshToken = refreshTokenRepository.findByEmail(email).orElseThrow(
                () -> new GeneralException(ErrorStatus._UNAUTHORIZED, "로그아웃된 사용자입니다.")
        );

        if (!storedRefreshToken.getTokenValue().equals(refreshToken)) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED, "토큰 정보가 일치하지 않습니다.");
        }

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND)
        );
        TokenInfo tokenInfo = generateToken(member);

        storedRefreshToken.updateToken(tokenInfo.getRefreshToken());
        refreshTokenRepository.save(storedRefreshToken);

        return tokenInfo;
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