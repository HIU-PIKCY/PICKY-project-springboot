package com.picky.domain.member.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignUpRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String nickname;

}
