package com.picky.domain.bookShelf.web.controller;

import com.picky.domain.book.web.dto.BookDetailDTO;
import com.picky.domain.bookShelf.web.dto.AddBookRequestDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO;
import com.picky.domain.bookShelf.service.BookShelfServiceImpl;
import com.picky.global.common.PagedMetaDTO;
import com.picky.global.common.ResponseDTO;
import com.picky.global.enums.ResponseCode;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-shelf")
@Tag(name = "내 서재")
public class BookShelfController {

    private final BookShelfServiceImpl bookShelfService;

    @GetMapping()
    public ResponseEntity<ResponseDTO<List<BookShelfResponseDTO>>> searchBooks(@Valid @RequestBody BookShelfRequestDTO request)
    {
        Pageable pageable = request.toPageable();
        Page<BookShelfResponseDTO> bookShelfPage = bookShelfService.getBookShelf(request, pageable);

        return ResponseEntity.ok(
                ResponseDTO.success(
                        ResponseCode.SUCCESS,
                        bookShelfPage.getContent(),
                        new PagedMetaDTO(
                                bookShelfPage.getNumber() + 1,
                                bookShelfPage.getSize(),
                                bookShelfPage.getTotalElements())
                ));
    }
/*
    @PostMapping()
    public ResponseEntity<BookDetailDTO> addBook(@Valid @RequestBody AddBookRequestDTO request){
        BookDetailDTO bookDetailDTO = bookShelfService.addBook(request);

        return ResponseEntity.ok(bookDetailDTO);
    }*/
}
