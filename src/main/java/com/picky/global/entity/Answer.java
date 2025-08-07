package com.picky.global.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "Answer")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Answer extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "question_pk", foreignKey = @ForeignKey(name = "fk_answer_question"))
  private Question question;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_answer_user"))
  private User user;

  @Schema(description = "내용", example = "민음사")
  @Column(nullable = false, length = 512)
  private String content;

  @Schema(description = "AI 생성 여부", example = "false")
  @Column()
  private Boolean isAiGenerated = false;
}
