package com.picky.domain.book.web.controller;

import com.picky.domain.book.service.BookServiceImpl;
import com.picky.domain.book.web.dto.BookDTO;
import com.picky.domain.book.web.dto.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.global.common.PagedMetaDTO;
import com.picky.global.common.ResponseDTO;
import com.picky.global.enums.ResponseCode;
import com.picky.global.error.NotFoundException;
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

    @GetMapping("/{bookId}")
    public ResponseEntity<ResponseDTO<BookDetailDTO>> getBookDetail(@PathVariable Long bookId)
    {
        Member member = null;
        //todo : 토큰으로부터 member 불러오기
        BookDetailDTO bookdto = bookService.getBookDetailById(bookId, member.getId());
        if(bookdto==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOOK);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, bookdto));
    }
}
