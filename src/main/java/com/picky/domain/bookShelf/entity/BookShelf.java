package com.picky.domain.bookShelf.entity;

import com.picky.domain.user.entity.User;
import com.picky.global.entity.BaseEntity;
import com.picky.domain.question.entity.Question;
import com.picky.global.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "BookShelf")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BookShelf extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "book_pk", foreignKey = @ForeignKey(name = "fk_book_shelf_book"))
  private Question question;

  @ManyToOne
  @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_book_shelf_user"))
  private User user;

  @Schema(description = "내용", example = "민음사")
  @Column(nullable = false, length = 512)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ReadingStatus Readingstatus = ReadingStatus.READING;
}
