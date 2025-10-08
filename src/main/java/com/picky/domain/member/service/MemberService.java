package com.picky.domain.member.service;

import com.picky.domain.member.entity.Member;
import com.picky.domain.member.web.dto.MemberResponseDTO;
import com.picky.domain.member.web.dto.MyMenuResponseDTO;
import com.picky.domain.member.web.dto.PatchMemberRequestDTO;

public interface MemberService {

    /**
     * member entity를 찾습니다.
     *
     * @param id 찾을 member의 id
     * @return Member entity
     */
    Member findById(Long id);

    /**
     * member entity를 수정합니다.
     *
     * @param member 수정할 member의 id
     * @param request 수정 요청 dto
     * @return MemberResponseDTO
     */
    MemberResponseDTO patchMember(Member member, PatchMemberRequestDTO request);

    MyMenuResponseDTO getMyMenu(Long memberId);

    void validateNickname(String nickname);

    boolean isNicknameAvailable(String nickname);

    /**
     * FCM 토큰을 업데이트합니다.
     *
     * @param memberId 업데이트할 member의 id
     * @param fcmToken 새로운 FCM 토큰
     */
    void updateFcmToken(Long memberId, String fcmToken);
}