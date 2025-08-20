package com.picky.domain.book.service;

import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

  public List<BookDTO> searchBooks(String keyword) {
    BookResponseDTO kakaoRes = restClient.get()
        .uri("https://dapi.kakao.com/v3/search/book?query={keyword}", keyword)
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
        book.setId(doc.getId());
        book.setTitle(doc.getTitle());
        book.setAuthor(doc.getAuthors());
        book.setPublisher(doc.getPublisher());
        book.setCoverImage(doc.getCoverImage());
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
    return results;
  }
}