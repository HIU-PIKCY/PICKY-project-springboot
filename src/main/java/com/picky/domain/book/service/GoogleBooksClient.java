package com.picky.domain.book.service;

import com.picky.domain.book.web.dto.BookDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleBooksClient {

        private final WebClient webClient;

        public BookDetailDTO getBookDetailByIsbn(String isbn) {
            // Google Books API URL
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:{isbn}";

            // 동기 호출
            Map<String, Object> response = webClient.get()
                    .uri(url, isbn)
                    .retrieve()
                    .bodyToMono(Map.class)  // Map으로 바로 받기
                    .block();

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) return null;

            Map<String, Object> volumeInfo = (Map<String, Object>) items.get(0).get("volumeInfo");

            return BookDetailDTO.builder()
                    .title((String) volumeInfo.get("title"))
                    .authors((List<String>) volumeInfo.get("authors"))
                    .publisher((String) volumeInfo.get("publisher"))
                    .coverImage(volumeInfo.get("imageLinks") != null
                            ? (String) ((Map<String, Object>) volumeInfo.get("imageLinks")).get("thumbnail")
                            : null)
                    .isbn(isbn)
                    .publishedAt((String) volumeInfo.get("publishedDate"))
                    .pageCount(volumeInfo.get("pageCount") != null ? ((Number) volumeInfo.get("pageCount")).intValue() : null)
                    .build();
        }
    }
