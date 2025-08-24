package com.picky.global.error;

import com.picky.global.enums.ResponseCode;
import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ResponseCode responseCode;

    public BaseException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public BaseException(ResponseCode responseCode, String message) {
        super(responseCode.getMessage() + ": " + message);
        this.responseCode = responseCode;
    }

    public BaseException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
    }

    public int getHttpStatus() {
        return responseCode.getHttpStatus().value();
    }}
