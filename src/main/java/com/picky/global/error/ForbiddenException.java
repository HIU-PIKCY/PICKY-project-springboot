package com.picky.global.error;

import com.picky.global.enums.ResponseCode;

public class ForbiddenException extends BaseException {
    public ForbiddenException(ResponseCode responseCode) {
        super(responseCode);
    }
}
