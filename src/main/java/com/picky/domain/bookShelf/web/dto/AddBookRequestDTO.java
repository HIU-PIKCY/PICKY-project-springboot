package com.picky.domain.bookShelf.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "서재에 책 추가를 위한 요청 객체")
public class AddBookRequestDTO {

    @Schema(description = "책 isbn", example = "13461112456")
    public Long isbn;

    @Schema(description = "독서 상태", example = "READING", nullable = true)
    public String status = "READING";
}
