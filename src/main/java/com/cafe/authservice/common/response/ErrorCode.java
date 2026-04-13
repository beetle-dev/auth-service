package com.cafe.authservice.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    AUTH_TOKEN_INVALID("A001", "유효하지 않은 토큰입니다."),
    AUTH_ACCESS_DENIED("A002", "권한이 없습니다."),
    AUTH_TOKEN_EXPIRE("A003", "토큰이 만료되었습니다."),
    DUPLICATE_EMAIL("A004", "중복된 이메일 입니다."),
    NOT_FOUND("A004", "해당 정보가 없습니다.");

    private final String code;
    private final String message;
}
