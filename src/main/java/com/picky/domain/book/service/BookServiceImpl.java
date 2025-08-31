package com.picky.domain.book.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.entity.QBook;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import com.picky.domain.bookShelf.service.BookShelfServiceImpl;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.AddBookRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
  private final WebClient webClient;
  private final JPAQueryFactory queryFactory;
  private final GoogleBooksClient googleBooksClient;
  private final BookRepository bookRepository;
    private final BookShelfServiceImpl bookShelfService;

    @Override
    public Book findByIsbn(String isbn) {
        BooleanExpression predicate = QBook.book.isbn.eq(isbn).and(QBook.book.status.eq(DataStatus.ACTIVATED));
        Optional<Book> bookEntity = bookRepository.findOne(predicate);
        return bookEntity.orElse(null);
    }

    @Override
    public Book save(Book book){
        return bookRepository.save(book);
    }


    @Value("${aladin.api.key}")
  private String aladinApiKey;

    @Override
    public Page<BookDTO> searchBooks(BookRequestDTO request, Pageable pageable) {
        String queryType = switch (request.getType().toLowerCase()) {
            case "title" -> "Title";
            case "author" -> "Author";
            default -> "Keyword";
        };

      int startIndex = pageable.getPageNumber() * pageable.getPageSize() + 1;
      int maxResults = pageable.getPageSize();

        String uri = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                + "?ttbkey={apiKey}&Query={query}&QueryType={type}&MaxResults={maxResults}&start={start}&SearchTarget=Book&output=js"+"&Version=20131101"+ "&Sort=SalesPoint";

        BookResponseDTO response = webClient.get()
                .uri(uri, aladinApiKey, request.getKeyword(), queryType, maxResults, startIndex)
                .retrieve()
                .bodyToMono(BookResponseDTO.class)
                .block();

      List<BookDTO> dtos = Optional.ofNullable(response)
              .map(BookResponseDTO::getItem) // response가 null이면 빈 리스트 처리
              .orElse(Collections.emptyList())
              .stream()
              .filter(Objects::nonNull)
              .map(item -> BookDTO.builder()
                          .title(item.getTitle())
                      .authors(item.getAuthor() != null ? List.of(item.getAuthor().split(",")) : Collections.emptyList())
                          .publisher(item.getPublisher())
                          .coverImage(item.getCover())
                          .isbn(item.getIsbn13())
                          .publishedAt(item.getPubDate())
                          .build())
              .collect(Collectors.toList());

      long total = (response != null && response.getTotalResults() != null)
              ? response.getTotalResults()
              : dtos.size();

    return new PageImpl<>(dtos, pageable, total);
  }

    @Override
    public BookDetailDTO getBookDetailByIsbn(String isbn, Long memberId) {
        QBookShelf bookShelf = QBookShelf.bookShelf;

            BookDetailDTO bookDTO = googleBooksClient.getBookDetailByIsbn(isbn);
            if (bookDTO == null) {
                throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);
            }

        BooleanExpression inLibrary = bookShelf.book.isbn.eq(isbn)
                .and(bookShelf.member.id.eq(memberId))
                .and(bookShelf.status.eq(DataStatus.ACTIVATED));

        BookShelf shelf = queryFactory
                .selectFrom(bookShelf)
                .where(inLibrary)
                .fetchFirst();

        bookDTO.setIsInLibrary(false); // 기본값 설정
        if (shelf != null) {
            bookDTO.setIsInLibrary(true);
            bookDTO.setReadingStatus(shelf.getReadingStatus());
        }

        return bookDTO;
    }

    @Transactional(readOnly = false)
    @Override
    public BookDetailDTO saveBookByIsbn(AddBookRequestDTO request, Long memberId) {
        BookDetailDTO bookDTO = googleBooksClient.getBookDetailByIsbn(request.isbn);

        Book book = findByIsbn(request.getIsbn());
        if (book == null) {
            // 새 책 생성
            book = Book.builder()
                    .isbn(bookDTO.getIsbn())
                    .title(bookDTO.getTitle())
                    .author(bookDTO.getAuthors() != null && !bookDTO.getAuthors().isEmpty()
                            ? bookDTO.getAuthors().get(0)
                            : null)
                    .publisher(bookDTO.getPublisher())
                    .coverImage(bookDTO.getCoverImage())
                    .publishedAt(LocalDate.parse(bookDTO.getPublishedAt()).atStartOfDay())
                    .pageCount(bookDTO.getPageCount())
                    .build();

            book = save(book);
        }

        // BookShelf에 추가 여부 확인
        BookShelf existingShelf = bookShelfService.findByMemberIdAndBookId(memberId, request.isbn);

        if (existingShelf != null) {
            throw new GeneralException(ErrorStatus.ALREADY_ADDED);
        }
            // 새 서재 엔티티 생성
            BookShelf shelf = BookShelf.builder()
                    .book(book)
                    .member(Member.builder().id(memberId).build())
                    .readingStatus(ReadingStatus.valueOf(request.status))
                    .build();

            bookShelfService.save(shelf);

        // DTO 반환
        BookDetailDTO detailDTO = BookDetailDTO.fromEntity(book);
        detailDTO.setIsInLibrary(true);
        detailDTO.setReadingStatus(ReadingStatus.valueOf(request.status));

        return detailDTO;
    }
}