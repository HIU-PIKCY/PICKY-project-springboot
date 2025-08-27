package com.picky.domain.member.repository;

import java.util.Optional;

import com.picky.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>,
    QuerydslPredicateExecutor<Member> {

    /**
     * 이메일로 회원을 조회합니다.
     * @param email 회원 이메일
     * @return 해당 회원
     */
    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);
}
