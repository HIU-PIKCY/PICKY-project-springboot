package com.picky.domain.questionLike.repository;

import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.Question;
import com.picky.domain.questionLike.entity.QuestionLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long>,
    QuerydslPredicateExecutor<QuestionLike> {

    Boolean existsByMemberAndQuestion(Member member, Question question);

    Optional<QuestionLike> findByMemberAndQuestion(Member member, Question question);

    int countByQuestion(Question question);
}
