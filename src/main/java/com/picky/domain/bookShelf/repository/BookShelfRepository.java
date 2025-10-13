package com.picky.domain.bookShelf.repository;

import com.picky.domain.bookShelf.entity.BookShelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookShelfRepository extends JpaRepository<BookShelf, Long>,
    QuerydslPredicateExecutor<BookShelf> {

    @Query(value = "SELECT * FROM book_shelf bs " +
                    "WHERE bs.book_id NOT IN (SELECT bs2.book_id FROM book_shelf bs2 WHERE bs2.member_id = :memberId) " +
                    "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<BookShelf> findRandomBook(@Param("memberId") Long memberId);

    List<BookShelf> findByMemberId(Long memberId);
}
