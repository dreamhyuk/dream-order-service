package com.dreamhyuk.dream_order.global.config;

import com.dreamhyuk.dream_order.global.jwt.JwtAuthenticationFilter;
import com.dreamhyuk.dream_order.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //💡시큐리티 필터 레이어에서 CORS를 최우선으로 처리하도록 주입
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                .csrf(AbstractHttpConfigurer::disable) // REST API이므로 csrf 비활성화
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        // 1. 브라우저 OPTIONS 예비 요청 무조건 허용
                        .requestMatchers(org.springframework.web.bind.annotation.RequestMethod.OPTIONS.name(), "/**").permitAll()

                        // 2. 인증이 절대 필요 없는 '가입' 및 '로그인' 주소 (가장 먼저 명시)
                        // 토큰 재발급(reissue)은 AuthController에 두는 것이 공통 관리상 유리합니다.
                        .requestMatchers("/api/customers/signup", "/api/customers/login").permitAll()
                        // 가게 검색 api는 로그인(token) 없이 누구나 접근 가능
                        .requestMatchers(HttpMethod.GET, "/api/customers/shops/**", "/api/customers/shops").permitAll()
                        .requestMatchers("/api/owners/signup", "/api/owners/login").permitAll()
                        .requestMatchers("/api/auth/reissue", "/api/categories").permitAll()

                        // 3.⭐[핵심 고치기] 가입/로그인을 제외한 나머지 모든 /api/customers/ 하위 요청은 무조건 CUSTOMER 권한 강제!
                        // 패턴을 명확하게 분리하여 우회 가능성을 차단
                        .requestMatchers("/api/customers/**", "/api/customers").hasRole("CUSTOMER")
                        .requestMatchers("/api/owners/**", "/api/owners").hasRole("OWNER")

                        // 4. 나머지는 다 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 끼워 넣음
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //💡시큐리티 전용 CORS 상세 설정 Bean 추가
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트엔드 Expo 웹 서버 주소를 정확히 허용합니다.
        configuration.addAllowedOrigin("http://localhost:8081");
        configuration.addAllowedOrigin("http://localhost:8082");
        // GET, POST, PUT, DELETE, OPTIONS 등 모든 HTTP 메서드를 허용합니다.
        configuration.addAllowedMethod("*");
        // Authorization, Content-Type 등 프론트가 보낼 모든 헤더를 허용합니다.
        configuration.addAllowedHeader("*");
        // 프론트엔드와 쿠키 및 토큰(Credentials)을 주고받을 수 있도록 허용합니다.
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 API 경로에 이 설정을 적용합니다.
        return source;
    }
}
