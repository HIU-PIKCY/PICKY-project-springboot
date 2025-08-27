package com.picky.domain.member.entity;

import com.picky.domain.answer.entity.Answer;
import com.picky.domain.bookShelf.entity.BookShelf;
import com.picky.domain.question.entity.Question;
import com.picky.domain.questionLike.entity.QuestionLike;
import com.picky.global.entity.BaseEntity;
import com.picky.domain.member.entity.enums.LoginType;

import jakarta.persistence.*;

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
public class Member extends BaseEntity {

  @Column(nullable = false, length = 50, unique = true)
  private String memberId;

  @Column(nullable = false, length = 256)
  private String name;

  @Column(nullable = false, length = 256)
  private String password;

  @Column(nullable = false, length = 256, unique = true)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private LoginType type = LoginType.GENERAL;

  @Column(length = 10)
  private String nickname;

  @ElementCollection(fetch = FetchType.EAGER)
  @Builder.Default
  private List<String> roles = new ArrayList<>();

  @OneToMany(mappedBy = "member")
  @Builder.Default
  private List<Answer> answers = new ArrayList<>();

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
  @Builder.Default
  private List<BookShelf> bookShelves = new ArrayList<>();

  @OneToMany(mappedBy = "member")
  @Builder.Default
  private List<Question> questions = new ArrayList<>();

  @OneToMany(mappedBy = "member")
  @Builder.Default
  private List<QuestionLike> questionLikes = new ArrayList<>();
}
