package com.picky.domain.auth.service;

import com.picky.domain.auth.web.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO loginOrSignUp(String firebaseToken);
}
