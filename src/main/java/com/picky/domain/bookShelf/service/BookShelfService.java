package com.picky.domain.bookShelf.service;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.GetBookShelfRequestDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfResponseDTO.GetBookShelfResponseDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.PatchBookShelfRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookShelfService {

    /**
     * id로 bookShelf entity를 찾습니다.
     *
     * @param id 찾을 bookShelf의 id
     * @return BookShelf entity
     */
    BookShelf findById(Long id);

    /**
     * isbn, member id로 bookShelf entity를 찾습니다.
     *
     * @param isbn book의 isbn
     * @param memberId bookShelf의 member id
     * @return BookShelf entity
     */
    BookShelf findByMemberIdAndBookId(Long memberId, String isbn);

    /**
     * bookShelf entity를 저장합니다.
     *
     * @param bookShelf 저장할 bookShelf 엔티티
     * @return BookShelf entity
     */
    BookShelf save(BookShelf bookShelf);

    /**
     * 내 서재 리스트를 조회합니다.
     *
     * @param request 조회를 위한 요청 객체
     * @param memberId 로그인한 사용자 id
     * @param pageable 페이징을 위한 객체
     * @return Page<BookShelfResponseDTO> 페이징 처리된 BookShelfResponseDTO 리스트
     */
    GetBookShelfResponseDTO getBookShelf(GetBookShelfRequestDTO request, Long memberId, Pageable pageable);

    /**
     * 내 서재에서 책을 삭제합니다.
     *
     * @param id 삭제할 bookShelf의 id
     */
    void deleteBookShelf(Long id);

    /**
     * 내 서재에 있는 책의 읽기 상태를 변경합니다.
     *
     * @param request 조회를 위한 요청 객체
     * @param id 변경할 bookShelf의 id
     * @return BookShelfResponseDTO
     */
    BookShelfResponseDTO patchBookStatus(Long id, PatchBookShelfRequestDTO request);
}
