package com.picky.domain.member.web.dto;

import com.picky.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "기본 유저 정보 DTO")
public class BaseUserDTO {

  @Schema(example = "1", description = "유저 pk값")
  private Long pk;

  @Schema(description = "이름", example = "박건우")
  private String name;

  @Schema(description = "닉네임", example = "누누")
  private String nickname;

  public BaseUserDTO(Member user) {
    pk = user.getPk();
    name = user.getName();
    nickname = user.getNickname();
  }
}
