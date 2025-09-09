//package com.picky.domain.member.service;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.picky.apiPayload.code.status.ErrorStatus;
//import com.picky.apiPayload.exception.GeneralException;
//import com.picky.domain.member.entity.Member;
//import com.picky.domain.member.repository.MemberRepository;
//import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class MemberServiceImpl implements MemberService {
//
//    private final MemberRepository memberRepository;
//
//    @Override
//    @Transactional
//    public void signUp(MemberSignUpRequestDTO memberSignUpRequestDto) {
//
//        if (memberRepository.existsByMemberId(memberSignUpRequestDto.getMemberId())) {
//            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXIST, "이미 사용 중인 아이디입니다.");
//        }
//
//        if (memberRepository.findByEmail(memberSignUpRequestDto.getEmail()).isPresent()) {
//            throw new GeneralException(ErrorStatus.MEMBER_ALREADY_EXIST, "이미 사용 중인 이메일입니다.");
//        }
//
//        Member member = Member.builder()
//                .memberId(memberSignUpRequestDto.getMemberId())
//                .email(memberSignUpRequestDto.getEmail())
//                .password(encodedPassword)
//                .name(memberSignUpRequestDto.getName())
//                .nickname(memberSignUpRequestDto.getNickname())
//                .roles(java.util.Collections.singletonList("USER"))
//                .build();
//        memberRepository.save(member);
//    }
//
//}
