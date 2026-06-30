package com.cafe.authservice.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    AUTH_TOKEN_INVALID("A001", "유효하지 않은 토큰입니다.",     HttpStatus.UNAUTHORIZED),
    AUTH_ACCESS_DENIED("A002", "권한이 없습니다.",             HttpStatus.FORBIDDEN),
    AUTH_TOKEN_EXPIRE ("A003", "토큰이 만료되었습니다.",        HttpStatus.UNAUTHORIZED),
    DUPLICATE_EMAIL   ("A004", "중복된 이메일 입니다.",         HttpStatus.CONFLICT),
    NOT_FOUND         ("A005", "해당 정보가 없습니다.",         HttpStatus.NOT_FOUND),
    VALIDATION_FAILED ("A006", "입력값이 유효하지 않습니다.",    HttpStatus.BAD_REQUEST),
    AUTH_INVALID_CREDENTIALS("A007", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    DISABLE_ACCOUNT("A008", "비활성화된 계정입니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;  // ← 추가
}
