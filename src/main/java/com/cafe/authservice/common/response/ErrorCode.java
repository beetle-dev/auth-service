package com.cafe.authservice.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT("C001", "잘못된 입력값입니다.");

    private final String code;
    private final String message;
}
