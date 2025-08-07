package com.picky.domain.questionLike.repository;

import com.picky.domain.questionLike.entity.QuestionLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionLikeRepository extends JpaRepository<QuestionLike, Long>,
    QuerydslPredicateExecutor<QuestionLike> {

}
