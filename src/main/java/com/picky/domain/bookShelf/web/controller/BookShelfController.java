package com.picky.domain.bookShelf.web.controller;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO;
import com.picky.domain.bookShelf.service.BookShelfServiceImpl;
import com.picky.domain.bookShelf.web.dto.PatchBookShelfRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.service.MemberServiceImpl;
import com.picky.global.common.PagedMetaDTO;
import com.picky.global.common.ResponseDTO;
import com.picky.global.enums.ResponseCode;
import com.picky.global.error.ForbiddenException;
import com.picky.global.error.NotFoundException;
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
    private final MemberServiceImpl memberService;

    @GetMapping()
    public ResponseEntity<ResponseDTO<List<BookShelfResponseDTO>>> getBookShelf(@Valid @ModelAttribute BookShelfRequestDTO request)
    {
        Pageable pageable = request.toPageable();
        Member member = memberService.findById(1L);
        //todo : 토큰으로부터 member 불러오기, 권한 확인
        Page<BookShelfResponseDTO> bookShelfPage = bookShelfService.getBookShelf(request, member.getId(), pageable);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> deleteBookShelf(@PathVariable Long id) {
        BookShelf bookShelf = bookShelfService.findById(id);
        if(bookShelf==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOOKSHELF);
        Member member = memberService.findById(1L);
        //todo : 토큰으로부터 member 불러오기, 권한 확인
        if (!bookShelf.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        bookShelfService.deleteBookShelf(bookShelf.getId());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ResponseDTO<BookShelfResponseDTO>> patchBookStatus(@PathVariable Long id, @Valid @RequestBody PatchBookShelfRequestDTO request) {
        BookShelf bookShelf = bookShelfService.findById(id);
        if(bookShelf==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOOKSHELF);
        Member member = memberService.findById(1L);
        //todo : 토큰으로부터 member 불러오기, 권한 확인
        if (!bookShelf.getMember().getId().equals(member.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        BookShelfResponseDTO dto = bookShelfService.patchBookStatus(bookShelf.getId(), request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
    }

}
