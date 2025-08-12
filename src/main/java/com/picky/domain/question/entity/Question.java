package com.picky.domain.question.entity;

import com.picky.domain.answer.entity.Answer;
import com.picky.domain.book.entity.Book;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.enums.QuestionType;
import com.picky.domain.questionLike.entity.QuestionLike;
import com.picky.global.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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
public class Question extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", foreignKey = @ForeignKey(name = "fk_question_book"))
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", foreignKey = @ForeignKey(name = "fk_question_member"))
  private Member member;

  @Schema(description = "내용", example = "민음사")
  @Column(nullable = false, length = 512)
  private String content;

  @Schema(description = "페이지 수", example = "46")
  private Integer pageNum;

  @Schema(description = "AI 생성 여부", example = "false")
  @Builder.Default
  private Boolean isAiGenerated = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private QuestionType type = QuestionType.THEME;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
  @Builder.Default
  private List<Answer> answers = new ArrayList<>();

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
  @Builder.Default
  private List<QuestionLike> questionLikes = new ArrayList<>();
}
