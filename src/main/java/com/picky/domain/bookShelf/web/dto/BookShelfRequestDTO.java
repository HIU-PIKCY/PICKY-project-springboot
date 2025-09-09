package com.picky.domain.bookShelf.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "내 서재 요청 객체")
public class BookShelfRequestDTO {

    @Getter
    @Setter
    public static class GetBookShelfRequestDTO {

        @Schema(description = "독서 상태 조건, 기본 값은 전체", example = "READING")
        public String status = "ALL";

        @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
        private Integer page = 1;

        @Schema(description = "페이지 크기", example = "20")
        private Integer size = 20;

        public Pageable toPageable() {
            int pageIndex = Math.max(this.page - 1, 0); // 0-based
            return PageRequest.of(pageIndex, size);
        }
    }

    @Getter
    public static class AddBookRequestDTO {

        @NotNull(message = "isbn은 필수값입니다.")
        @Schema(description = "책 isbn", example = "9788934986225")
        public String isbn;

        @Schema(description = "독서 상태", example = "READING", nullable = true)
        @Pattern(regexp = "READING|COMPLETED", message = "허용되지 않는 상태입니다.")
        public String status = "READING";
    }

    public static class PatchBookShelfRequestDTO {

        @NotNull(message = "독서 상태는 필수값입니다.")
        @Schema(description = "독서 상태", example = "COMPLETED")
        @Pattern(regexp = "READING|COMPLETED", message = "허용되지 않는 상태입니다.")
        public String status;
    }

}