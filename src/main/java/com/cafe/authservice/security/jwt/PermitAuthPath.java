package com.cafe.authservice.security.jwt;

import java.util.List;

public final class PermitAuthPath {

    public static final String LOGIN = "/login";
    public static final String SIGNUP = "/signup";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
    public static final String HEALTH_CHECK = "/actuator/health";

    public static final List<String> permitAuthPaths =
            List.of("/", LOGIN, SIGNUP, SWAGGER_UI, API_DOCS, HEALTH_CHECK);
}
