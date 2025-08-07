package com.picky.domain.questionLike.entity;

import com.picky.domain.question.entity.Question;
import com.picky.domain.member.entity.Member;
import com.picky.global.entity.BaseEntity;
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
@Table(name = "QuestionLike")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionLike extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "question_pk", foreignKey = @ForeignKey(name = "fk_question_like_question"))
  private Question question;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_question_like_user"))
  private Member member;
}
