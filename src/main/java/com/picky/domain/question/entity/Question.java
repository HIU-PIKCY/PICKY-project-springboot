package com.picky.domain.question.entity;

import com.picky.domain.answer.entity.Answer;
import com.picky.domain.book.entity.Book;
import com.picky.domain.member.entity.Member;
import com.picky.domain.question.entity.enums.QuestionType;
import com.picky.domain.questionLike.entity.QuestionLike;
import com.picky.global.entity.BaseEntity;
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
import org.hibernate.annotations.BatchSize;

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

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 512)
  private String content;

  private int pageNum;

  private int views;

  @Builder.Default
  private Boolean isAiGenerated = false;

  private String aiSummary;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private QuestionType type = QuestionType.CHARACTER;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
  @Builder.Default
  @BatchSize(size = 100)
  private List<Answer> answers = new ArrayList<>();

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
  @Builder.Default
  @BatchSize(size = 100)
  private List<QuestionLike> questionLikes = new ArrayList<>();

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
  @Builder.Default
  private List<AiHashtag> aiHashtags = new ArrayList<>();

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Builder.Default
    private List<QuestionKeyword> questionKeywords = new ArrayList<>();

  public void updateAIAnalysis(String summary, List<AiHashtag> hashtags) {
    this.aiSummary = summary;
    this.aiHashtags.clear();
    if (hashtags != null) {
      this.aiHashtags.addAll(hashtags);
    }
  }
}
