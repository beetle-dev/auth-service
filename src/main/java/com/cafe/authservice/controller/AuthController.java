package com.cafe.authservice.controller;

import com.cafe.authservice.common.response.CommonResponse;
import com.cafe.authservice.domain.Users;
import com.cafe.authservice.dto.UserCreateReqDto;
import com.cafe.authservice.dto.UserResDto;
import com.cafe.authservice.dto.UsersSearchDto;
import com.cafe.authservice.security.jwt.JwtTokenProvider;
import com.cafe.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<?>> logout(HttpServletRequest request,
                                                    HttpServletResponse response) {

        String authorization = request.getHeader("Authorization");
        String accessToken = authorization.split(" ")[1];
        String uuid = jwtTokenProvider.blacklistAccessToken(accessToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // todo 운영에서 true로 변경 필요
                .sameSite("none")
                .maxAge(0)
                .path("/")
                .build();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=UTF-8");
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        jwtTokenProvider.deleteRefreshToken(uuid);

        return ResponseEntity.ok(CommonResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<?>> getUserInfo(@RequestHeader("X-User-Id") String uuid) {

        Users user = authService.getUserInfo(UUID.fromString(uuid));

        return ResponseEntity.ok(CommonResponse.ok(UserResDto.from(user)));
    }

    @PostMapping("/user")
    public ResponseEntity<CommonResponse<?>> register(@Valid @RequestBody UserCreateReqDto newUser) { // todo 클라이언트로부터 받을 수 잇는 형태는?

        authService.register(newUser);

        return ResponseEntity.ok(CommonResponse.ok());
    }

    @GetMapping("/users")
    public ResponseEntity<CommonResponse<?>> getUsers(@Valid @ModelAttribute UsersSearchDto reqDto) {

        return ResponseEntity.ok(CommonResponse.ok(authService.getUsers(reqDto)));
    }

    @PatchMapping(
            "/users/{uuid}")
    public ResponseEntity<CommonResponse<?>> modifyUser(@PathVariable("uuid") UUID uuid,
                                                        @Valid @RequestBody UserCreateReqDto reqDto,
                                                        @RequestHeader("X-User-Id") String requesterId,
                                                        @RequestHeader("X-User-Role") String requesterRole) {

        authService.modifyUser(uuid, reqDto, requesterId, requesterRole);

        return ResponseEntity.ok(CommonResponse.ok());
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestHeader("X-User-Role") String requesterRole) throws Exception { // todo 주석 정리
        // refreshToken 쿠키 검증 → DB에서 uuid로 name/role 조회 → 새 access token 발급
        // JwtTokenProvider.reissueAccessToken 로직 활용 또는 분리
        jwtTokenProvider.reissueAccessToken(request, response, requesterRole);
        return null; // response에 직접 write
    }
}
