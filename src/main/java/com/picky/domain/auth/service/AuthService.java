package com.picky.domain.auth.service;

import com.picky.domain.auth.web.dto.AuthResponseDTO;
import com.picky.domain.member.web.dto.MemberSignUpRequestDTO;

public interface AuthService {
    AuthResponseDTO login(String firebaseToken);
    AuthResponseDTO signUp(String firebaseToken, MemberSignUpRequestDTO memberSignUpRequestDTO);
}
