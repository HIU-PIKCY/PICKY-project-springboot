package com.picky.domain.question.entity;

import com.picky.domain.book.entity.Book;
import com.picky.domain.member.entity.Member;
import com.picky.global.entity.BaseEntity;
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
@Table(name = "Question")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Question extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "book_pk", foreignKey = @ForeignKey(name = "fk_question_book"))
  private Book book;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_question_user"))
  private Member user;

  @Schema(description = "내용", example = "민음사")
  @Column(nullable = false, length = 512)
  private String content;

  @Schema(description = "페이지 수", example = "46")
  @Column()
  private Integer pageNum;

  @Schema(description = "AI 생성 여부", example = "false")
  @Column()
  private Boolean isAiGenerated = false;
}
