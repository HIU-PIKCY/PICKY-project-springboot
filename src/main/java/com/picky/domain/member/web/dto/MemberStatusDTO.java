package com.picky.domain.member.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "사용자 활동 통계 DTO")
public class MemberStatusDTO {

    @Schema(example = "12", description = "총 독서량")
    private long totalBooks;

    @Schema(example = "21", description = "작성한 질문 수")
    private long questions;

    @Schema(example = "8", description = "작성한 답변 수")
    private long answers;
}
