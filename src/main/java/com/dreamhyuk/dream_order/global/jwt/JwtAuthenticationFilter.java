package com.dreamhyuk.dream_order.global.jwt;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.global.exception.InvalidTokenException;
import com.dreamhyuk.dream_order.global.exception.TokenExpiredException;
import com.dreamhyuk.dream_order.global.userdetails.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        try {
            // 2. 토큰이 있고 유효하다면 인증 정보 설정
            if (token != null && jwtProvider.validateToken(token)) {
                Authentication auth = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (TokenExpiredException | InvalidTokenException e) {
            // 필터단에서 발생한 예외는 여기서 처리하거나 다음 필터로 넘깁니다.
            // 일단은 인증 정보 없이 다음 체인으로 넘기고, SecurityConfig에서 처리하게 합니다.
            log.warn("JWT 인증 실패: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Authorization: Bearer <token>" 형태의 토큰을 꺼내옵니다.
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰을 이용해 Spring Security의 Authentication 객체를 생성합니다.
    private Authentication getAuthentication(String token) {
        Long memberId = jwtProvider.getMemberIdFromToken(token);
        MemberRole role = jwtProvider.getRoleFromToken(token);

        // Spring Security에서 사용하는 SimpleGrantedAuthority로 권한 설정
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

        // CustomUserDetails 객체 생성 (Principal로 사용될 객체)
        // password는 토큰 인증이므로 null 혹은 빈 문자열을 넣습니다.
        CustomUserDetails userDetails = new CustomUserDetails(
                memberId,
                null, // email이 필요하다면 토큰에서 추출해서 넣으세요.
                "",
                "",
                role,
                authorities
        );

        // Principal에 userId(또는 User객체), Credentials(비번)은 null, 권한 리스트 전달
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}
