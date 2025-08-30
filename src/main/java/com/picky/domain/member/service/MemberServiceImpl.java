package com.picky.domain.member.service;

import com.picky.domain.member.entity.Member;
import com.picky.domain.member.entity.QMember;
import com.picky.domain.member.repository.MemberRepository;
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
}
