package com.picky.domain.book.service;

import com.picky.domain.book.entity.Book;
import com.picky.domain.book.web.dto.BookResponseDTO.BookSearchResponseDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.AddBookRequestDTO;

public interface BookService {


    /**
     * book entity를 찾습니다.
     *
     * @param isbn 찾을 책의 isbn
     * @return Book entity
     */
    Book findByIsbn(String isbn);

    /**
     * book entity를 저장합니다.
     *
     * @param book 저장할 book 엔티티
     * @return Book entity
     */
    Book save(Book book);

    /**
     * google api를 이용하여 책을 검색합니다.
     *
     * @param request 검색을 위한 요청 객체
     * @return Page<BookDTO> 페이징 처리된 BookDTO 리스트
     */
    BookSearchResponseDTO searchBooks(BookRequestDTO request);

    /**
     * 책을 저장하고, 내 서재에 추가합니다.
     *
     * @param request 책 추가를 위한 요청 객체
     * @param memberId 사용자 id
     * @return BookDetailDTO 책 상세 정보
     */
    BookDetailDTO saveBookByIsbn(AddBookRequestDTO request, Long memberId);

    /**
     * 책을 상세 조회합니다.
     *
     * @param isbn 상세 조회할 책의 isbn
     * @param memberId 사용자 id
     * @return BookDetailDTO 책 상세 정보
     */
    BookDetailDTO getBookDetailByIsbn(String isbn, Long memberId);
}
