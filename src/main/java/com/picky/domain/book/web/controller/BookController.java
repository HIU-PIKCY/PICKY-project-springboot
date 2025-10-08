package com.picky.domain.book.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.auth.CustomerUserDetails;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.service.BookService;
import com.picky.domain.book.web.dto.BookResponseDTO.BookSearchResponseDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookSaveResponseDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDetailDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookIdResponseDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.AddBookRequestDTO;
import com.picky.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@Tag(name = "책")
public class BookController {
    private final BookService bookService;

    @Operation(summary = "키워드로 책 검색 API", description = "키워드와 타입에 따른 검색 결과 목록을 조회합니다.")
    @GetMapping("/search")
    public ApiResponse<BookSearchResponseDTO> searchBooks(@Valid @ModelAttribute BookRequestDTO request)
    {
        BookSearchResponseDTO searchBooks = bookService.searchBooks(request);
        return ApiResponse.onSuccess(searchBooks);
    }

    @Operation(summary = "책 상세 조회 API", description = "isbn으로 책을 상세 조회합니다.")
    @GetMapping("/{isbn}")
    public ApiResponse<BookDetailDTO> getBookDetail(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @PathVariable String isbn)
    {
        Member currentMember = customerUserDetails.getMember();
        BookDetailDTO bookdto = bookService.getBookDetailByIsbn(isbn, currentMember.getId());
        if(bookdto==null) throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);

        return ApiResponse.onSuccess(bookdto);
    }

    @Operation(summary = "서재에 책 추가 API", description = "책을 내 서재에 추가합니다.")
    @PostMapping()
    public ApiResponse<BookSaveResponseDTO> saveBookByIsbn(
            @AuthenticationPrincipal CustomerUserDetails customerUserDetails,
            @Valid @RequestBody AddBookRequestDTO request){

        Member currentMember = customerUserDetails.getMember();
        BookSaveResponseDTO dto = bookService.saveBookByIsbn(request, currentMember.getId());

        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "ISBN으로 책 ID 조회 API", description = "ISBN으로 데이터베이스에 저장된 책의 ID를 조회합니다.")
    @GetMapping("/isbn/{isbn}")
    public ApiResponse<BookIdResponseDTO> getBookIdByIsbn(
            @Parameter(description = "조회할 책의 ISBN", example = "9788936434120")
            @PathVariable String isbn) {

        Book book = bookService.findByIsbn(isbn);

        if (book == null) {
            throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);
        }

        BookIdResponseDTO response = BookIdResponseDTO.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .build();

        return ApiResponse.onSuccess(response);
    }
}
