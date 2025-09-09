package com.picky.domain.member.service;

import com.picky.domain.member.entity.Member;

public interface MemberService {

    /**
     * member entity를 찾습니다.
     *
     * @param id 찾을 member의 id
     * @return Member entity
     */
    Member findById(Long id);
}