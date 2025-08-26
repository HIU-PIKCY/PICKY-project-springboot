package com.picky.domain.question.repository;

import com.picky.domain.question.entity.Question;
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
}
