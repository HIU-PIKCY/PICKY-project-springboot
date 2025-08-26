package com.picky.domain.answer.repository;

import com.picky.domain.answer.entity.Answer;
import com.picky.domain.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long>,
    QuerydslPredicateExecutor<Answer> {

    int countByQuestion(Question question);
}
