package com.picky.apiPayload.exception.handler;

import com.picky.apiPayload.ApiResponse;
import com.picky.apiPayload.code.ErrorReasonDTO;
import com.picky.apiPayload.exception.GeneralException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("400", "Validation Failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("400", "Invalid request body or format", null));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<ErrorReasonDTO>> handleGeneralException(GeneralException ex) {
        ErrorReasonDTO errorReason = ex.getErrorReason();

        return ResponseEntity
                .status(ex.getErrorReasonHttpStatus().getHttpStatus())  // 상태 코드 동적 설정
                .body(ApiResponse.onFailure(errorReason.getCode(), errorReason));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorReasonDTO>> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.onFailure("400", "Error: " + ex.getMessage(), null));
    }

}
