package com.picky.domain.book.web.controller;

import com.picky.domain.book.service.BookServiceImpl;
import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;
import com.picky.global.common.PagedMetaDTO;
import com.picky.global.common.ResponseDTO;
import com.picky.global.enums.ResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "책")
public class BookController {
  private final BookServiceImpl bookService;

  @GetMapping("/search")
  public ResponseEntity<ResponseDTO<List<BookDTO>>> searchBooks(@Valid @ModelAttribute BookRequestDTO request)
 {
     Pageable pageable = request.toPageable();
     Page<BookDTO> searchBooksPage = bookService.searchBooks(request, pageable);

     return ResponseEntity.ok(
             ResponseDTO.success(
                     ResponseCode.SUCCESS,
                     searchBooksPage.getContent(),
             new PagedMetaDTO(
                     searchBooksPage.getNumber() + 1,
                     searchBooksPage.getSize(),
                     searchBooksPage.getTotalElements())
     ));
  }
}
