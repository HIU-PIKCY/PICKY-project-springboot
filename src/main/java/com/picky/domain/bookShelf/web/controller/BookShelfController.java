package com.picky.domain.bookShelf.web.controller;

import com.picky.domain.bookShelf.dto.BookListDTO;
import com.picky.domain.bookShelf.dto.BookListRequestDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-shelf")
@Tag(name = "내 서재")
public class BookShelfController {

    private final BookShelfServiceImpl bookShelfService;

    @GetMapping()
    public ResponseEntity<ResponseDTO<List<BookListDTO>>> searchBooks(@Valid @ModelAttribute BookListRequestDTO request)
    {
        Pageable pageable = request.toPageable();
        Page<BookListDTO> bookShelfPage = bookShelfService.getBookShelf(request, pageable);

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
}
