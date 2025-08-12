package com.picky.domain.answer.entity;

import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.Question;
import com.picky.global.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", foreignKey = @ForeignKey(name = "fk_answer_question"))
  private Question question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_answer_member"))
  private Member member;

  @Schema(description = "내용", example = "민음사")
  @Column(nullable = false, length = 512)
  private String content;

  @Schema(description = "AI 생성 여부", example = "false")
  @Builder.Default
  private Boolean isAiGenerated = false;
}