package com.picky.domain.questionLike.repository;

import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.Question;
import com.picky.domain.questionLike.entity.QuestionLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long>,
        QuerydslPredicateExecutor<QuestionLike> {

    Boolean existsByMemberAndQuestion(Member member, Question question);

    Optional<QuestionLike> findByMemberAndQuestion(Member member, Question question);

    int countByQuestion(Question question);

    /**
     * 특정 사용자가 좋아요한 질문 목록을 조회합니다.
     * 질문 정보, 책 정보, 질문 작성자 정보를 포함하여 조회합니다.
     */
    @Query("SELECT ql FROM QuestionLike ql " +
            "LEFT JOIN FETCH ql.question q " +
            "LEFT JOIN FETCH q.book b " +
            "LEFT JOIN FETCH q.member qm " +
            "WHERE ql.member.id = :memberId " +
            "ORDER BY ql.createdAt DESC")
    List<QuestionLike> findByMemberIdWithQuestionAndBookAndQuestionMember(@Param("memberId") Long memberId);
}