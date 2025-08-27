package com.picky.domain.member.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignUpRequestDTO {

    private String memberId;
    private String email;
    private String password;
    private String name;
    public String nickname;

}
