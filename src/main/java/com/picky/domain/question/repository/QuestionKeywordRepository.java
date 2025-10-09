package com.picky.domain.question.repository;

import com.picky.domain.question.entity.Question;
import com.picky.domain.question.entity.QuestionKeyword;
import com.picky.domain.question.entity.enums.Keyword;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionKeywordRepository extends JpaRepository<QuestionKeyword, Long> {

    interface KeywordCount {
        Keyword getKeyword();
        Long getCount();
    }

    @Query("SELECT qk.keyword as keyword, COUNT(qk.keyword) as count " +
        "FROM QuestionKeyword qk " +
        "JOIN qk.question q " +
        "WHERE q.createdAt >= :startDate AND q.createdAt < :endDate " +
        "GROUP BY qk.keyword " +
        "ORDER BY count DESC")
    List<KeywordCount> findKeywordCountsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 특정 사용자의 가장 많이 사용된 키워드를 조회합니다.
     */
    @Query("SELECT qk FROM QuestionKeyword qk " +
            "JOIN FETCH qk.question q " +
            "WHERE q.member.id = :memberId")
    List<QuestionKeyword> findByQuestionMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 키워드를 가진 다른 사용자들의 질문을 조회합니다.
     */
    @Query("SELECT qk FROM QuestionKeyword qk " +
            "JOIN FETCH qk.question q " +
            "WHERE qk.keyword = :keyword " +
            "AND q.member.id != :excludeMemberId " +
            "AND q.book IS NOT NULL")
    List<QuestionKeyword> findByKeywordAndQuestionMemberIdNot(
            @Param("keyword") Keyword keyword,
            @Param("excludeMemberId") Long excludeMemberId
    );

    /**
    해당 키워드를 사용한 모든 사용자의 질문을 가져옵니다.
     */
    @Query("SELECT qk FROM QuestionKeyword qk " +
            "JOIN FETCH qk.question q " +
            "WHERE qk.keyword = :keyword " +
            "AND q.book IS NOT NULL")
    List<QuestionKeyword> findByKeyword(@Param("keyword") Keyword keyword);

    List<QuestionKeyword> findByQuestionId(Long questionId);
    List<QuestionKeyword> findByKeywordInAndQuestionIdNot(List<Keyword> keywords, Long questionId);
}
