package com.picky.domain.bookShelf.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "서재에 책 추가를 위한 요청 객체")
public class AddBookRequestDTO {

    @NotNull(message = "isbn은 필수값입니다.")
    @Schema(description = "책 isbn", example = "9788934986225")
    public String isbn;

    @Schema(description = "독서 상태", example = "READING", nullable = true)
    @Pattern(regexp = "READING|COMPLETED", message = "허용되지 않는 상태입니다.")
    public String status = "READING";
}
