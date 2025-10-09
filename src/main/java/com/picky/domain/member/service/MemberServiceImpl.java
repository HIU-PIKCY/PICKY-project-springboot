package com.picky.domain.member.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.entity.QMember;
import com.picky.domain.member.repository.MemberRepository;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import com.picky.domain.member.web.dto.MemberStatusDTO;
import com.picky.domain.member.web.dto.MyMenuResponseDTO;
import com.picky.domain.member.web.dto.PatchMemberRequestDTO;
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member findById(Long id) {
        BooleanExpression predicate = QMember.member.id.eq(id).and(QMember.member.status.eq(DataStatus.ACTIVATED));
        Optional<Member> memberEntity = memberRepository.findOne(predicate);
        return memberEntity.orElse(null);
    }


    public void validateNickname(String nickname) {
        if (memberRepository.existsByNicknameAndStatus(nickname,  DataStatus.ACTIVATED)) {
            throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_USED);
        }
    }

    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNicknameAndStatus(nickname, DataStatus.ACTIVATED);
    }

    @Transactional
    @Override
    public MemberResponseDTO patchMember(Member member, PatchMemberRequestDTO request) {
            if (request.getNickname() != null) {
                validateNickname(request.getNickname()); // 중복이면 예외 발생
                member.setNickname(request.getNickname());
            }
            if (request.getProfileImg() != null) {
                member.setProfileImg(request.getProfileImg());
            }

            memberRepository.save(member);

            return MemberResponseDTO.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .name(member.getName())
                    .nickname(member.getNickname())
                    .profileImg(member.getProfileImg())
                    .build();
    }

    @Override
    public MyMenuResponseDTO getMyMenu(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new GeneralException((ErrorStatus.MEMBER_NOT_FOUND)));

        long totalBooks = member.getBookShelves().size();
        long questions = member.getQuestions().size();
        long answers = member.getAnswers().size();

        MemberResponseDTO memberResponseDTO = MemberResponseDTO.fromEntity(member);
        MemberStatusDTO memberStatusDTO = MemberStatusDTO.builder()
                .totalBooks(totalBooks)
                .questions(questions)
                .answers(answers)
                .build();

        return MyMenuResponseDTO.builder()
                .user(memberResponseDTO)
                .stats(memberStatusDTO)
                .build();
    }

    @Override
    @Transactional
    public void updateFcmToken(Long memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        member.updateFcmToken(fcmToken);
    }
}