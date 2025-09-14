package com.picky.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 512)
    private String tokenValue;

    public RefreshToken(String email, String tokenValue) {
        this.email = email;
        this.tokenValue = tokenValue;
    }

    public void updateToken(String refreshToken) { // 여기에서 해도 될 지
        this.tokenValue = refreshToken;
    }
}
