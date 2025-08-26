package com.picky.domain.question.repository;

import com.picky.domain.question.entity.Question;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>,
        QuerydslPredicateExecutor<Question> {

    @Query("""
        SELECT q
        FROM Question q
        JOIN FETCH q.book b
        WHERE q.id = :questionId
        """)
    Optional<Question> findWithBookById(@Param("questionId") Long questionId);
    /**
     * 특정 사용자가 작성한 질문 목록을 조회합니다. (기본 질문 정보와 책 정보만 조회)
     */
    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN FETCH q.book b " +
            "WHERE q.member.id = :memberId " +
            "ORDER BY q.createdAt DESC")
    List<Question> findByMemberIdWithBook(@Param("memberId") Long memberId);

    /**
     * 특정 질문들의 좋아요 수를 조회합니다.
     */
    @Query("SELECT q.id, COUNT(ql) FROM Question q " +
            "LEFT JOIN q.questionLikes ql " +
            "WHERE q.id IN :questionIds " +
            "GROUP BY q.id")
    List<Object[]> countLikesByQuestionIds(@Param("questionIds") List<Long> questionIds);

    /**
     * 특정 질문들의 답변 수를 조회합니다.
     */
    @Query("SELECT q.id, COUNT(a) FROM Question q " +
            "LEFT JOIN q.answers a " +
            "WHERE q.id IN :questionIds " +
            "GROUP BY q.id")
    List<Object[]> countAnswersByQuestionIds(@Param("questionIds") List<Long> questionIds);
}
