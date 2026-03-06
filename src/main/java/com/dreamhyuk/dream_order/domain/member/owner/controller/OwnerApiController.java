package com.dreamhyuk.dream_order.domain.member.owner.controller;

import com.dreamhyuk.dream_order.domain.auth.dto.AuthRequestDto;
import com.dreamhyuk.dream_order.domain.auth.dto.AuthResponseDto;
import com.dreamhyuk.dream_order.domain.auth.service.AuthService;
import com.dreamhyuk.dream_order.domain.member.owner.service.OwnerCommand;
import com.dreamhyuk.dream_order.domain.member.owner.service.OwnerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners")
public class OwnerApiController {

    private final OwnerService ownerService;
    private final AuthService authService;

    /** 회원 가입 */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody OwnerRequestDto.SignUp request) {
        OwnerCommand.SignUp command = request.toCommand();

        Long id = ownerService.saveOwner(command);

        return ResponseEntity.ok(id);
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto.Token> login(
            @Valid @RequestBody AuthRequestDto.Login request, HttpServletResponse response) {

        AuthResponseDto.Token tokenDto = authService.login(request);

        //refresh token 을 위한 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                .httpOnly(true) //JavaScript에서 쿠키 접근 불가 (XSS 방지)
                .secure(false) //HTTPS 연결에서만 쿠키 전송 (테스트를 위해 일단 false로 해두겠다)
                .path("/") //모든 경로에서 쿠키 유효
                .maxAge(Duration.ofDays(14)) //14일
                .sameSite("Strict") //CSRF 공격 방지
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(tokenDto);
    }
}
