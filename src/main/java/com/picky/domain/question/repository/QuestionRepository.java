package com.picky.domain.question.repository;

import com.picky.domain.book.entity.Book;
import com.picky.domain.question.entity.Question;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>,
        QuerydslPredicateExecutor<Question> {

    // getQuestionDetail을 위한 쿼리
    @Query("SELECT q FROM Question q JOIN FETCH q.book b JOIN FETCH q.member m WHERE q.id = :questionId")
    Optional<Question> findByIdWithBookAndMember(@Param("questionId") Long questionId);

    // getQuestionList를 위한 쿼리
    @Query("SELECT q FROM Question q JOIN FETCH q.book b WHERE q.book.id = :bookId")
    List<Question> findByBookIdWithBook(@Param("bookId") Long bookId);

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

    @Modifying(clearAutomatically = true)
    @Query("update Question q set q.views = q.views + 1 where q.id = :id")
    int increaseViews(@Param("id") Long id);

    @Query("SELECT q FROM Question q WHERE q.createdAt >= :startDate AND q.createdAt < :endDate ORDER BY SIZE(q.answers) DESC, q.views DESC")
    List<Question> findTopQuestionBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT q.book FROM Question q WHERE q.createdAt >= :startDate AND q.createdAt < :endDate GROUP BY q.book ORDER BY COUNT(q) DESC")
    List<Book> findTopBooksByQuestionBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
