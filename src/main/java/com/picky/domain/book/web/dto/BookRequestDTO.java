package com.picky.domain.book.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "책 검색을 위한 필터링 요청 객체")
public class BookRequestDTO {

    @NotNull(message = "키워드는 필수값입니다.")
    @Schema(description = "검색 키워드", example = "동물")
    public String keyword;

    @Schema(description = "검색 타입, 기본은 전체 검색", example = "title")
    public String type = "all";

    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    private Integer page = 1;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;
}
