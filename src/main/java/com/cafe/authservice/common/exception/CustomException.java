package com.cafe.authservice.common.exception;

import com.cafe.authservice.common.response.ErrorCode;

public class CustomException extends RuntimeException{

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
