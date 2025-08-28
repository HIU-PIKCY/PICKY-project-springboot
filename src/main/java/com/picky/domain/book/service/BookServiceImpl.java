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
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
  private final RestClient restClient;
  private final JPAQueryFactory queryFactory;

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


      BookResponseDTO response = restClient.get()
              .uri(uri, keywordQuery, startIndex, maxResults, googleApiKey)
              .retrieve()
              .body(BookResponseDTO.class);

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

    public BookDetailDTO getBookDetailById(Long bookId, Long memberId) {
        QBook book = QBook.book;
        QBookShelf bookShelf = QBookShelf.bookShelf;

        Book bookEntity = queryFactory
                .selectFrom(book)
                .where(book.id.eq(bookId))
                .fetchOne();

        if (bookEntity == null) {
            return null;
        }

        // 서재 정보 조회
        BooleanExpression inLibrary = bookShelf.book.id.eq(bookId)
                .and(bookShelf.member.id.eq(memberId))
                .and(bookShelf.status.eq(DataStatus.ACTIVATED));

        BookShelf shelf = queryFactory
                .selectFrom(bookShelf)
                .where(inLibrary)
                .fetchFirst();

        // Entity 기반 DTO 생성
        BookDetailDTO dto = BookDetailDTO.fromEntity(bookEntity);

        if (shelf != null) {
            dto.setIsInLibrary(true);
            dto.setReadingStatus(shelf.getReadingStatus());
        }
        return dto;
    }
}