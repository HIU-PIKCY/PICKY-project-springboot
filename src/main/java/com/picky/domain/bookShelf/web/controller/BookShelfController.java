package com.picky.domain.bookShelf.web.controller;

import com.picky.apiPayload.ApiResponse;
import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.code.status.SuccessStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.auth.web.CurrentUser;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.service.BookShelfService;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO.GetBookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.GetBookShelfRequestDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.PatchBookShelfRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.domain.member.service.MemberService;
import com.picky.global.common.PagedMetaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-shelf")
@Tag(name = "내 서재")
public class BookShelfController {

    private final BookShelfService bookShelfService;

    @Operation(summary = "내 서재 목록 조회 API", description = "내 서재 목록을 조회합니다.")
    @GetMapping()
    public ApiResponse<GetBookShelfResponseDTO> getBookShelf(@Valid @ModelAttribute GetBookShelfRequestDTO request)
    {
        Pageable pageable = request.toPageable();
        Member member = CurrentUser.get();

        GetBookShelfResponseDTO bookShelf = bookShelfService.getBookShelf(request, member.getId(), pageable);

        return ApiResponse.onSuccess(bookShelf);
    }

    @Operation(summary = "내 서재 책 삭제 API", description = "내 서재에서 책을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<SuccessStatus> deleteBookShelf(@PathVariable Long id) {
        BookShelf bookShelf = bookShelfService.findById(id);
        if(bookShelf==null) throw new GeneralException(ErrorStatus.BOOKSHELF_NOT_FOUND);
        Member member = CurrentUser.get();

        if (!bookShelf.getMember().getId().equals(member.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        bookShelfService.deleteBookShelf(bookShelf.getId());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @Operation(summary = "내 서재 책 상태 변경 API", description = "내 서재에서 책의 읽기 상태를 변경합니다.")
    @PatchMapping("/{id}")
    public ApiResponse<BookShelfResponseDTO> patchBookStatus(@PathVariable Long id, @Valid @RequestBody PatchBookShelfRequestDTO request) {
        BookShelf bookShelf = bookShelfService.findById(id);
        if(bookShelf==null) throw new GeneralException(ErrorStatus.BOOKSHELF_NOT_FOUND);
        Member member = CurrentUser.get();

        if (!bookShelf.getMember().getId().equals(member.getId())) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        BookShelfResponseDTO dto = bookShelfService.patchBookStatus(bookShelf.getId(), request);
        return ApiResponse.onSuccess(dto);
    }

}
