package com.picky.global.error;

import com.picky.global.common.ResponseDTO;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseDTO<Void>> handleBaseException(BaseException e) {
        return ResponseEntity.status(e.getHttpStatus()) // ✅ 예외의 HTTP 상태 코드 적용
                .body(new ResponseDTO<>(e.getResponseCode().getCode(), e.getMessage(), null, null));
    }
}
