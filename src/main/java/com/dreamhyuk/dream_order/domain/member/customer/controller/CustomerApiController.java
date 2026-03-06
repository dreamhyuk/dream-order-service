package com.dreamhyuk.dream_order.domain.member.customer.controller;

import com.dreamhyuk.dream_order.domain.auth.dto.AuthRequestDto;
import com.dreamhyuk.dream_order.domain.auth.dto.AuthResponseDto;
import com.dreamhyuk.dream_order.domain.auth.service.AuthService;
import com.dreamhyuk.dream_order.domain.member.customer.service.CustomerCommand;
import com.dreamhyuk.dream_order.domain.member.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerApiController {

    private final CustomerService customerService;
    private final AuthService authService;

    /** 회원 가입 */
    @PostMapping("/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody CustomerRequestDto.SignUp request) {
        CustomerCommand.SingUp command = request.toCommand();

        Long id = customerService.saveCustomer(command);

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


    /** 조회 */
//    @GetMapping("/me")
//    public String status()
}
