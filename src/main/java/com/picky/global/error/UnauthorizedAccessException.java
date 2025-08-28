package com.picky.global.error;

import com.picky.global.enums.ResponseCode;

public class UnauthorizedAccessException extends BaseException {
    public UnauthorizedAccessException(ResponseCode responseCode) {
        super(responseCode);
    }
}
