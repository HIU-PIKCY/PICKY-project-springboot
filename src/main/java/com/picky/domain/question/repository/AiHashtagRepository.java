package com.picky.domain.question.repository;

import com.picky.domain.question.entity.AiHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiHashtagRepository extends JpaRepository<AiHashtag, Long> {

}
