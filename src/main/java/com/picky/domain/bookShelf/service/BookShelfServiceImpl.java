package com.picky.domain.bookShelf.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.QBook;
import com.picky.domain.bookShelf.repository.BookShelfRepository;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.GetBookShelfRequestDTO;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.PatchBookShelfRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.entity.QMember;
import com.picky.domain.member.service.MemberServiceImpl;
import com.picky.global.enums.DataStatus;
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

    @Override
    public BookShelf findById(Long id) {
        BooleanExpression predicate = QBookShelf.bookShelf.id.eq(id).and(QBookShelf.bookShelf.status.eq(DataStatus.ACTIVATED));
        Optional<BookShelf> bookShelfEntity = bookShelfRepository.findOne(predicate);
        return bookShelfEntity.orElse(null);
    }

    @Override
    public BookShelf findByMemberIdAndBookId(Long memberId, String isbn) {
        BooleanExpression predicate = QBookShelf.bookShelf.member.id.eq(memberId).and(QBook.book.isbn.eq(isbn)).and(QBookShelf.bookShelf.status.eq(DataStatus.ACTIVATED));
        Optional<BookShelf> bookShelfEntity = bookShelfRepository.findOne(predicate);
        return bookShelfEntity.orElse(null);
    }

    @Override
    public BookShelf save(BookShelf bookShelf){
        return bookShelfRepository.save(bookShelf);
    }

    @Override
    public Page<BookShelfResponseDTO> getBookShelf(GetBookShelfRequestDTO request, Long memberId, Pageable pageable) {
        QBookShelf bookShelf = QBookShelf.bookShelf;
        QMember member = QMember.member;
        QBook book = QBook.book;

        BooleanBuilder builder = new BooleanBuilder()
                .and(bookShelf.status.eq(DataStatus.ACTIVATED));

        if (memberId != null) {
            Member memberEntity = memberService.findById(memberId);
            if (memberEntity == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
            builder.and(member.id.eq(memberId));
        }

        if (!"ALL".equalsIgnoreCase(request.getStatus())) {
            System.out.println("Status: '" + request.getStatus() + "'");
            builder.and(bookShelf.readingStatus.eq(
                    ReadingStatus.valueOf(request.getStatus().toUpperCase())
            ));
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

    @Override
    @Transactional(readOnly = false)
    public void deleteBookShelf(Long id) {
        BookShelf bookShelf = findById(id);

        bookShelf.setStatus(DataStatus.DEACTIVATED);
        bookShelfRepository.save(bookShelf);
    }

    @Override
    @Transactional(readOnly = false)
    public BookShelfResponseDTO patchBookStatus(Long id, PatchBookShelfRequestDTO request) {
        BookShelf bookShelf = findById(id);

        bookShelf.setReadingStatus(ReadingStatus.valueOf(request.status));
        BookShelf savedBookShelf = bookShelfRepository.save(bookShelf);

        // DTO 반환
        return BookShelfResponseDTO.fromBookShelf(savedBookShelf);
    }

    }
