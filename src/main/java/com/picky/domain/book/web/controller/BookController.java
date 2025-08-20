package com.picky.domain.book.web.controller;

import com.picky.domain.book.service.BookServiceImpl;
import com.picky.domain.book.web.dto.BookDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "책")
public class BookController {
  private final BookServiceImpl bookService;

  @GetMapping("/search")
  public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String keyword) {
    return ResponseEntity.ok(bookService.searchBooks(keyword));
  }
}
