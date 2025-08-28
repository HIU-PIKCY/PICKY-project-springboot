package com.picky.domain.bookShelf.service;

import com.picky.domain.book.entity.QBook;
import com.picky.domain.bookShelf.repository.BookShelfRepository;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.entity.QMember;
import com.picky.domain.member.service.MemberServiceImpl;
import com.picky.global.enums.DataStatus;
import com.picky.global.enums.ResponseCode;
import com.picky.global.error.NotFoundException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookShelfServiceImpl implements BookShelfService{
    private final JPAQueryFactory queryFactory;
    private final MemberServiceImpl memberService;
    private final BookShelfRepository bookShelfRepository;

    public BookShelf findByMemberIdAndBookId(Long memberId, String isbn) {
        BooleanExpression predicate = QBookShelf.bookShelf.member.id.eq(memberId).and(QBook.book.isbn.eq(isbn)).and(QBookShelf.bookShelf.status.eq(DataStatus.ACTIVATED));
        Optional<BookShelf> bookShelfEntity = bookShelfRepository.findOne(predicate);
        return bookShelfEntity.orElse(null);
    }

    public BookShelf save(BookShelf bookShelf){
        return bookShelfRepository.save(bookShelf);
    }

    public Page<BookShelfResponseDTO> getBookShelf(BookShelfRequestDTO request, Pageable pageable) {
        QBookShelf bookShelf = QBookShelf.bookShelf;
        QMember member = QMember.member;
        QBook book = QBook.book;

        BooleanBuilder builder = new BooleanBuilder()
                .and(bookShelf.status.eq(DataStatus.ACTIVATED));

        if (request.getMemberId() != null) {
            Member memberEntity = memberService.findById(request.getMemberId());
            if (memberEntity == null) throw new NotFoundException(ResponseCode.NOT_FOUND_MEMBER);
            builder.and(member.id.eq(request.getMemberId()));
        }

        if (request.getStatus() != null) {
            builder.and(bookShelf.readingStatus.eq(ReadingStatus.valueOf(request.getStatus())));
        }
        // content
        List<BookShelf> content = queryFactory
                .selectFrom(bookShelf)
                .leftJoin(bookShelf.book, book).fetchJoin()
                .leftJoin(bookShelf.member, member).fetchJoin()
                .where(builder)
                .orderBy(bookShelf.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<BookShelfResponseDTO> dtos = content.stream()
                .map(BookShelfResponseDTO::fromBookShelf)
                .toList();

        // total count
        long total = Optional.ofNullable(
                queryFactory.select(bookShelf.count())
                        .from(bookShelf)
                        .leftJoin(bookShelf.book, book)
                        .leftJoin(bookShelf.member, member)
                        .where(builder)
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(dtos, pageable, total);
    }

    }
