package com.picky.domain.bookShelf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "내 서재 조회를 위한 요청 객체")
public class BookListRequestDTO {

    @Schema(description = "회원 id", example = "1")
    public Long memberId;

    @Schema(description = "독서 상태 정렬 조건", example = "READING", nullable = true)
    public String status = "all";

    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    private Integer page = 1;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;

    public Pageable toPageable() {
        int pageIndex = Math.max(this.page - 1, 0); // 0-based
        return PageRequest.of(pageIndex, size);
    }
}
