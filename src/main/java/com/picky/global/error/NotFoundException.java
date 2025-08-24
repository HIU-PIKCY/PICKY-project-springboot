package com.picky.global.error;

import com.picky.global.enums.ResponseCode;

public class NotFoundException extends BaseException {
    public NotFoundException(ResponseCode responseCode) {
        super(responseCode);
    }
    public NotFoundException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }}
