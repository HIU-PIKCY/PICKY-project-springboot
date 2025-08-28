package com.picky.domain.bookShelf.repository;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.global.enums.DataStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookShelfRepository extends JpaRepository<BookShelf, Long>,
    QuerydslPredicateExecutor<BookShelf> {
    Optional<BookShelf> findByMemberIdAndBookIdAndStatus(Long memberId, Long bookId, DataStatus status);

}
