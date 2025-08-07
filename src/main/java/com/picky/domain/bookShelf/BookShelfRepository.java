package com.picky.domain.bookShelf;

import com.picky.global.entity.BookShelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BookShelfRepository extends JpaRepository<BookShelf, Long>,
    QuerydslPredicateExecutor<BookShelf> {

}
