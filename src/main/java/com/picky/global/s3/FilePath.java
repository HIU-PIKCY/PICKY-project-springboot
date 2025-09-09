package com.picky.global.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FilePath {
    PROFILE("profile");
    // 더 생기면 여기 추가
    private final String path;
}
