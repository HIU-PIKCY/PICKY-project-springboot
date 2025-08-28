package com.picky.domain.book.service;

import com.picky.domain.book.entity.Book;
import com.picky.domain.book.entity.QBook;
import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.repository.BookShelfRepository;
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.Projections;
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
}