package com.picky.domain.member.entity;

import com.picky.global.entity.BaseEntity;
import com.picky.global.enums.LoginType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "User")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

  @Column(nullable = false, length = 256)
  private String name;

  @Column(nullable = false, length = 256)
  private String password;

  @Column(nullable = false, length = 256)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private LoginType type = LoginType.GENERAL;

  @Column(length = 10)
  private String nickname;

}
