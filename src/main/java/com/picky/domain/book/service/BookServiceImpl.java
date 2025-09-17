package com.picky.domain.book.service;

import com.picky.apiPayload.code.status.ErrorStatus;
import com.picky.apiPayload.exception.GeneralException;
import com.picky.domain.book.entity.Book;
import com.picky.domain.book.entity.QBook;
import com.picky.domain.book.repository.BookRepository;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookDetailDTO;
import com.picky.domain.book.web.dto.BookRequestDTO;
import com.picky.domain.book.web.dto.BookResponseDTO;
import com.picky.domain.book.web.dto.BookResponseDTO.BookSearchResponseDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.bookShelf.entity.QBookShelf;
import com.picky.domain.bookShelf.entity.enums.ReadingStatus;
import com.picky.domain.bookShelf.service.BookShelfServiceImpl;
import com.picky.domain.bookShelf.web.dto.BookShelfRequestDTO.AddBookRequestDTO;
import com.picky.domain.member.entity.Member;
import com.picky.global.enums.DataStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
  private final WebClient webClient;
  private final JPAQueryFactory queryFactory;
  private final BookRepository bookRepository;
    private final BookShelfServiceImpl bookShelfService;

    @Override
    public Book findByIsbn(String isbn) {
        BooleanExpression predicate = QBook.book.isbn.eq(isbn).and(QBook.book.status.eq(DataStatus.ACTIVATED));
        Optional<Book> bookEntity = bookRepository.findOne(predicate);
        return bookEntity.orElse(null);
    }

    @Override
    public Book save(Book book){
        return bookRepository.save(book);
    }


    @Value("${aladin.api.key}")
  private String aladinApiKey;

    @Override
    public BookSearchResponseDTO searchBooks(BookRequestDTO request) {
        String queryType = switch (request.getType().toLowerCase()) {
            case "title" -> "Title";
            case "author" -> "Author";
            default -> "Keyword";
        };

        int start = request.getPage() * request.getSize() + 1; // Aladin API는 1부터 시작
        int maxResults = request.getSize();

        String uri = "https://www.aladin.co.kr/ttb/api/ItemSearch.aspx"
                + "?ttbkey={apiKey}&Query={query}&QueryType={type}&MaxResults={maxResults}&start={start}&SearchTarget=Book&output=js"+"&Version=20131101"+ "&Sort=SalesPoint";

        BookResponseDTO response = webClient.get()
                .uri(uri, aladinApiKey, request.getKeyword(), queryType, maxResults, start)
                .retrieve()
                .bodyToMono(BookResponseDTO.class)
                .block();

      List<BookDTO> dtos = Optional.ofNullable(response)
              .map(BookResponseDTO::getItem) // response가 null이면 빈 리스트 처리
              .orElse(Collections.emptyList())
              .stream()
              .filter(Objects::nonNull)
              .map(item -> {
                  // authors 필드에서 괄호 안 내용 제거하고 trim
                  List<String> authors = Optional.ofNullable(item.getAuthor())
                          .map(s -> Arrays.stream(s.split(","))
                                  .map(String::trim)
                                  .map(a -> a.replaceAll("\\(.*?\\)", "")) // (지은이), (옮긴이) 등 제거
                                  .toList()
                          ).orElse(Collections.emptyList());

                  return BookDTO.builder()
                          .title(item.getTitle())
                          .authors(authors)
                          .publisher(item.getPublisher())
                          .coverImage(item.getCover())
                          .isbn(item.getIsbn13())
                          .publishedAt(item.getPubDate())
                          .build();
              })
              .collect(Collectors.toList());

        int totalCount = Optional.ofNullable(response)
                .map(BookResponseDTO::getTotalResults)
                .orElse(dtos.size());

        boolean hasNext = totalCount > request.getPage() * request.getSize();

        BookSearchResponseDTO result = new BookSearchResponseDTO();
        result.setItems(dtos);
        result.setHasNext(hasNext);

        return result;
  }

    @Override
    public BookDetailDTO getBookDetailByIsbn(String isbn, Long memberId) {
        QBookShelf bookShelf = QBookShelf.bookShelf;

        // 알라딘 상세조회 API 호출
        String uri = "https://www.aladin.co.kr/ttb/api/ItemLookUp.aspx"
                + "?ttbkey={apiKey}&itemIdType=ISBN&ItemId={isbn}&output=js&Version=20131101";

        BookResponseDTO response = webClient.get()
                .uri(uri, aladinApiKey, isbn)
                .retrieve()
                .bodyToMono(BookResponseDTO.class)
                .block();

        if (response == null || response.getItem() == null || response.getItem().isEmpty()) {
            throw new GeneralException(ErrorStatus.BOOK_NOT_FOUND);
        }

        // 첫 번째 결과만 사용
        BookResponseDTO.Item item = response.getItem().get(0);

        // authors 가공 (검색 API에서 했던 것과 동일하게)
        List<String> authors = Optional.ofNullable(item.getAuthor())
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(a -> a.replaceAll("\\(.*?\\)", "")) // (지은이), (옮긴이) 제거
                        .toList()
                ).orElse(Collections.emptyList());

        // DTO 변환
        BookDetailDTO bookDTO = BookDetailDTO.builder()
                .title(item.getTitle())
                .authors(authors)
                .publisher(item.getPublisher())
                .coverImage(item.getCover())
                .isbn(item.getIsbn13())
                .publishedAt(item.getPubDate())
                .pageCount(item.getSubInfo() != null ? item.getSubInfo().getItemPage() : null) // 알라딘 응답에 있으면
                .build();

        BooleanExpression inLibrary = bookShelf.book.isbn.eq(isbn)
                .and(bookShelf.member.id.eq(memberId))
                .and(bookShelf.status.eq(DataStatus.ACTIVATED));

        BookShelf shelf = queryFactory
                .selectFrom(bookShelf)
                .where(inLibrary)
                .fetchFirst();

        bookDTO.setIsInLibrary(false); // 기본값 설정
        if (shelf != null) {
            bookDTO.setIsInLibrary(true);
            bookDTO.setReadingStatus(shelf.getReadingStatus());
        }

        return bookDTO;
    }

    @Transactional(readOnly = false)
    @Override
    public BookDetailDTO saveBookByIsbn(AddBookRequestDTO request, Long memberId) {
        BookDetailDTO bookDTO = getBookDetailByIsbn(request.isbn, memberId);

        Book book = findByIsbn(request.getIsbn());
        if (book == null) {
            // 새 책 생성
            book = Book.builder()
                    .isbn(bookDTO.getIsbn())
                    .title(bookDTO.getTitle())
                    .author(bookDTO.getAuthors() != null && !bookDTO.getAuthors().isEmpty()
                            ? bookDTO.getAuthors().get(0)
                            : null)
                    .publisher(bookDTO.getPublisher())
                    .coverImage(bookDTO.getCoverImage())
                    .publishedAt(LocalDate.parse(bookDTO.getPublishedAt()).atStartOfDay())
                    .pageCount(bookDTO.getPageCount())
                    .build();

            book = save(book);
        }

        // BookShelf에 추가 여부 확인
        BookShelf existingShelf = bookShelfService.findByMemberIdAndBookId(memberId, request.isbn);

        if (existingShelf != null) {
            throw new GeneralException(ErrorStatus.ALREADY_ADDED);
        }
            // 새 서재 엔티티 생성
            BookShelf shelf = BookShelf.builder()
                    .book(book)
                    .member(Member.builder().id(memberId).build())
                    .readingStatus(ReadingStatus.valueOf(request.status))
                    .build();

            bookShelfService.save(shelf);

        // DTO 반환
        BookDetailDTO detailDTO = BookDetailDTO.fromEntity(book);
        detailDTO.setIsInLibrary(true);
        detailDTO.setReadingStatus(ReadingStatus.valueOf(request.status));

        return detailDTO;
    }
}