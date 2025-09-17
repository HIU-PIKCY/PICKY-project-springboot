package com.picky.domain.member.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.entity.QMember;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import com.picky.domain.member.web.dto.PatchMemberRequestDTO;
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member findById(Long id) {
        BooleanExpression predicate = QMember.member.id.eq(id).and(QMember.member.status.eq(DataStatus.ACTIVATED));
        Optional<Member> memberEntity = memberRepository.findOne(predicate);
        return memberEntity.orElse(null);
    }


    public void validateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_USED);
        }
    }

    @Override
    public MemberResponseDTO patchMember(Member member, PatchMemberRequestDTO request) {

        if (request.getNickname() != null) {
            validateNickname(request.getNickname()); // 중복이면 예외 발생
            member.setNickname(request.getNickname());
        }
        if (request.getProfileImg() != null) {
            member.setProfileImg(request.getProfileImg());
        }

        Member savedMember = memberRepository.save(member);

        return MemberResponseDTO.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .nickname(savedMember.getNickname())
                .profileImg(savedMember.getProfileImg())
                .build();
    }
}