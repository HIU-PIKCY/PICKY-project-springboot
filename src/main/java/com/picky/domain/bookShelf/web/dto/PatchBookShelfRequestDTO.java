package com.picky.domain.bookShelf.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "책 읽기 상태 변경을 위한 요청 객체")
public class PatchBookShelfRequestDTO {

    @NotNull(message = "독서 상태는 필수값입니다.")
    @Schema(description = "독서 상태", example = "READING")
    @Pattern(regexp = "READING|COMPLETED", message = "허용되지 않는 상태입니다.")
    public String status;
}
