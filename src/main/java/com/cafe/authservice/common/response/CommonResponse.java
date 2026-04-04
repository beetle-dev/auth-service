package com.cafe.authservice.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final String code;
    private final String message;
    private final T data;

    private static final String SUCCESS_CODE = "SUCCESS";

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(SUCCESS_CODE, null, data);
    }

    public static <T> CommonResponse<T> ok() {
        return new CommonResponse<>(SUCCESS_CODE, null, null);
    }

    public static <T> CommonResponse<T> fail(String code, String message) {
        return new CommonResponse<>(code, message, null);
    }

    public static <T> CommonResponse<T> fail(ErrorCode errorCode) {
        return new CommonResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }
}
