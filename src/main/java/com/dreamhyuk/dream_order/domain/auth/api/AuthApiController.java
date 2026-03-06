package com.dreamhyuk.dream_order.domain.auth.api;

import com.dreamhyuk.dream_order.domain.auth.dto.AuthRequestDto;
import com.dreamhyuk.dream_order.domain.auth.dto.AuthResponseDto;
import com.dreamhyuk.dream_order.domain.auth.service.AuthService;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<Long> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getMemberId(), userDetails.getRole());
        //일단 id 값을 반환
        return ResponseEntity.ok(userDetails.getMemberId());
    }

    /** 토큰 재발급 */
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponseDto.Token> refresh(
            @CookieValue(name = "refreshToken") String refreshToken, //쿠키에서 추출
            HttpServletResponse response) {

        AuthResponseDto.Token newTokenSet = authService.refresh(refreshToken);

        //새로운 refresh token 을 쿠키에 설정
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newTokenSet.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        //body 에는 access token 만 응답
        return ResponseEntity.ok(newTokenSet);
    }

/*    @PostMapping("/logout")
    public String logout(HttpSession session) {

    }*/


}
