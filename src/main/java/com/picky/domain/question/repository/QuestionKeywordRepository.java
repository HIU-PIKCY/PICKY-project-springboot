package com.picky.domain.question.repository;

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
}
