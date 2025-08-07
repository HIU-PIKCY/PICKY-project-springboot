package com.picky.global.entity;

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
public class QuestionLike extends BaseEntity{
  @ManyToOne
  @JoinColumn(name = "question_pk", foreignKey = @ForeignKey(name = "fk_question_like_question"))
  private Question question;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_question_like_user"))
  private User user;
}
