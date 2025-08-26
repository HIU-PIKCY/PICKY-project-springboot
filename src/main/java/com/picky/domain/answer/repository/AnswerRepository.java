package com.picky.domain.answer.repository;

import com.picky.domain.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>,
        QuerydslPredicateExecutor<Answer> {

    /**
     * 특정 사용자가 작성한 답변 목록을 조회합니다. (질문 정보 포함)
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
            "LEFT JOIN FETCH a.question q " +
            "LEFT JOIN FETCH q.book b " +
            "WHERE a.member.id = :memberId " +
            "ORDER BY a.createdAt DESC")
    List<Answer> findByMemberIdWithQuestionAndBook(@Param("memberId") Long memberId);
}