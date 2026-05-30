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

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 💡 1. CORS 예비 요청(OPTIONS)은 토큰 검증 없이 무조건 다음 필터로 통과시킵니다.
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 💡 2. [수정] SecurityConfig에서 permitAll() 해둔 오픈 API 주소 목록에 '가게 검색 API' 추가!
        if (path.startsWith("/api/customers/signup") || path.startsWith("/api/customers/login") ||
                path.startsWith("/api/owners/signup") || path.startsWith("/api/owners/login") ||
                path.startsWith("/api/auth/reissue") || path.startsWith("/api/categories") ||
                (path.startsWith("/api/customers/shops") && "GET".equalsIgnoreCase(method))) { // 👈 가게 검색 GET 요청 추가

            // ⭐ [핵심 포인트]
            // 비로그인 상태(오픈 API)여도 혹시 헤더에 토큰이 묻어있다면(예: 로그인 세션이 남아있는 유저)
            // 엘라스틱서치나 이후 로직에서 유저 정보를 쓸 수 있도록 인증 객체는 넣어주는 게 좋습니다.
            String token = resolveToken(request);
            if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
                Authentication auth = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // 토큰이 있든 없든 오픈 API이므로 에러를 터뜨리지 않고 무조건 다음 필터로 통과시킵니다.
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 헤더에서 토큰 추출
        String token = resolveToken(request);

        try {
            // 💡 4. 보호된 주소인데 토큰이 아예 없다면 즉시 예외를 발생시킵니다.
            if (token == null) {
                throw new IllegalArgumentException("인증 토큰이 누락되었습니다.");
            }

            // 토큰이 있고 유효하다면 인증 정보 설정
            if (jwtProvider.validateToken(token)) {
                Authentication auth = getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 💡 성공 시에만 다음 필터 체인으로 이동합니다.
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            // 토큰 만료, 잘못된 토큰, 토큰 누락 등 모든 인증 실패는 이곳에서 걸러집니다.
            log.warn("JWT 인증 실패: {}", e.getMessage());

            // 💡 5. [핵심] 다음 체인으로 넘기지 않고 여기서 즉시 브라우저에 401 에러 코드를 내려꽂아 버립니다.
            sendErrorResponse(response, "인증에 실패했습니다: " + e.getMessage());
            return; // 메서드를 종료하여 filterChain.doFilter() 호출을 차단합니다.
        }
    }

    // 브라우저에 명시적인 401 에러 반환을 위한 헬퍼 메서드
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태코드 설정
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
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
        CustomUserDetails userDetails = new CustomUserDetails(
                memberId,
                null,
                "",
                "",
                role,
                authorities
        );

        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }
}

/*
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
*/
