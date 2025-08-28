package com.picky.domain.book.service;

import com.picky.domain.book.entity.Book;
import com.picky.domain.book.entity.QBook;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.service.BookShelfService;
import com.picky.domain.bookShelf.service.BookShelfServiceImpl;
import com.picky.domain.bookShelf.web.dto.AddBookRequestDTO;
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

    public Book findByIsbn(String isbn) {
        BooleanExpression predicate = QBook.book.isbn.eq(isbn).and(QBook.book.status.eq(DataStatus.ACTIVATED));
        Optional<Book> bookEntity = bookRepository.findOne(predicate);
        return bookEntity.orElse(null);
    }

    public Book save(Book book){
        return bookRepository.save(book);
    }


    @Value("${google.api.key}")
  private String googleApiKey;

  public Page<BookDTO> searchBooks(BookRequestDTO request, Pageable pageable) {
      String keywordQuery = switch (request.getType().toLowerCase()) {
          case "title" -> "intitle:" + request.getKeyword();
          case "author" -> "inauthor:" + request.getKeyword();
          default -> request.getKeyword(); // 전체 검색
      };

      int startIndex = pageable.getPageNumber() * pageable.getPageSize();
      int maxResults = pageable.getPageSize();

      String uri = "https://www.googleapis.com/books/v1/volumes"
              + "?q={query}&startIndex={startIndex}&maxResults={maxResults}&key={apiKey}";


      BookResponseDTO response = webClient.get()
              .uri(uri, keywordQuery, startIndex, maxResults, googleApiKey)
              .retrieve()
              .bodyToMono(BookResponseDTO.class)
              .block();

      List<BookDTO> dtos = Optional.ofNullable(response)
              .map(BookResponseDTO::getItems) // response가 null이면 빈 리스트 처리
              .orElse(Collections.emptyList())
              .stream()
              .filter(Objects::nonNull)
              .map(item -> {
                  BookResponseDTO.VolumeInfo info = item.getVolumeInfo();
                  if (info == null) {
                      return BookDTO.builder()
                              .authors(Collections.emptyList())
                              .build();
                  }
                  return BookDTO.builder()
                          .title(info.getTitle())
                          .authors(info.getAuthors() != null ? info.getAuthors() : Collections.emptyList())
                          .publisher(info.getPublisher())
                          .coverImage(info.getImageLinks() != null ? info.getImageLinks().getThumbnail() : null)
                          .isbn(extractIsbn(info))
                          .publishedAt(info.getPublishedDate())
                          .pageCount(info.getPageCount() != null ? info.getPageCount() : 0)
                          .build();
              })
              .collect(Collectors.toList());

      long total = (response != null && response.getTotalItems() != null)
              ? response.getTotalItems()
              : dtos.size();

    return new PageImpl<>(dtos, pageable, total);
  }

    private String extractIsbn(BookResponseDTO.VolumeInfo info) {
        if (info.getIndustryIdentifiers() == null) return null;
        return info.getIndustryIdentifiers().stream()
                .filter(id -> "ISBN_13".equals(id.getType()))
                .findFirst().map(BookResponseDTO.IndustryIdentifiers::getIdentifier)
                .orElse(null);
    }

    public BookDetailDTO getBookDetailByIsbn(String isbn, Long memberId) {
        QBookShelf bookShelf = QBookShelf.bookShelf;

            BookDetailDTO bookDTO = googleBooksClient.getBookDetailByIsbn(isbn);
            if (bookDTO == null) {
                throw new RuntimeException("책 정보를 찾을 수 없습니다.");
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
        if (existingShelf == null) {
            // 새 서재 엔티티 생성
            BookShelf shelf = BookShelf.builder()
                    .book(book)
                    .member(Member.builder().id(memberId).build())
                    .readingStatus(request.status)
                    .build();

            bookShelfService.save(shelf);
        }

        // DTO 반환
        BookDetailDTO detailDTO = BookDetailDTO.fromEntity(book);
        detailDTO.setIsInLibrary(true);
        detailDTO.setReadingStatus(request.status);

        return detailDTO;
    }
}