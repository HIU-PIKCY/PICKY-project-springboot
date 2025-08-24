package com.picky.domain.book.service;

import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

  @Value("${kakao.api.key}")
  private String kakaoApiKey;

  public Page<BookDTO> searchBooks(BookRequestDTO request, Pageable pageable) {
      String targetParam = switch (request.getType().toLowerCase()) {
          case "title" -> "title";
          case "author" -> "person";
          default -> null; // 전체 검색
      };
      int page = pageable.getPageNumber() + 1; // Spring Pageable는 0부터, Kakao API는 1부터
      int size = pageable.getPageSize();

      String uri = "https://dapi.kakao.com/v3/search/book?query={keyword}&page={page}&size={size}";
      if (targetParam != null) {
          uri += "&target=" + targetParam;
      }

    BookResponseDTO kakaoRes = restClient.get()
        .uri(uri, request.getKeyword(), page, size)
        .header("Authorization", "KakaoAK " + kakaoApiKey)
        .retrieve()
        .body(BookResponseDTO.class);

    /*
    BookResponseDTO naverRes = restClient.get()
        .uri("https://openapi.naver.com/v1/search/book.json?query={keyword}", keyword)
        .header("X-Naver-Client-Id", naverClientId)
        .header("X-Naver-Client-Secret", naverClientSecret)
        .retrieve()
        .body(BookResponseDTO.class);
*/
    List<BookDTO> results = new ArrayList<>();

    if (kakaoRes.getDocuments() != null) {
      for (BookResponseDTO.Document doc : kakaoRes.getDocuments()) {
        BookDTO book = new BookDTO();
        book.setTitle(doc.getTitle());
        book.setAuthor(doc.getAuthors());
        book.setPublisher(doc.getPublisher());
        book.setCoverImage(doc.getThumbnail());
        book.setIsbn(doc.getIsbn());
        if (doc.getDatetime() != null) {
          OffsetDateTime odt = OffsetDateTime.parse(
              doc.getDatetime(),
              DateTimeFormatter.ISO_OFFSET_DATE_TIME
          );
          book.setPublishedAt(odt.toLocalDateTime()); // LocalDateTime 변환 후 저장
        }        results.add(book);
      }
    }
/*
    if (naverRes != null && naverRes.getItems() != null) {
      for (NaverBookResponse.Item item : naverRes.getItems()) {
        BookDTO b = new BookDTO();
        b.setId(item.getId());
        b.setTitle(item.getTitle());
        b.setAuthors(Arrays.asList(item.getAuthor().split(",")));
        b.setPublisher(item.getPublisher());
        b.setThumbnail(item.getImage());
        b.setIsbn(item.getIsbn());
        b.setPublishDate(item.getPubdate());
        results.add(b);
      }
    }
*/
      long total = 0L;
      if (kakaoRes != null && kakaoRes.getMeta() != null) {
          total = kakaoRes.getMeta().getPageable_count(); // 페이징 가능한 결과 수
      }
    return new PageImpl<>(results, pageable, total);
  }
}