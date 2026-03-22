package com.dreamhyuk.dream_order.global.config;

import com.dreamhyuk.dream_order.global.jwt.JwtAuthenticationFilter;
import com.dreamhyuk.dream_order.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
//                .csrf(AbstractHttpConfigurer::disable) // REST API이므로 csrf 비활성화
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
                .authorizeHttpRequests(auth -> auth
                        // 1. 공통 인증 및 토큰 관련 (누구나 접근 가능)
                        // 토큰 재발급(reissue)은 AuthController에 두는 것이 공통 관리상 유리합니다.
                        .requestMatchers("/api/auth/reissue").permitAll()

                        // 2. 고객(Customer) 도메인 설정
                        // 가입과 로그인은 인증 없이 접근 가능해야 합니다.
                        .requestMatchers("/api/customers/signup", "/api/customers/login").permitAll()
                        // 그 외의 모든 고객 API는 CUSTOMER 권한 필요
                        .requestMatchers("/api/customers/**").hasRole("CUSTOMER")

                        // 3. 사장님(Owner) 도메인 설정
                        // 사장님 가입과 로그인도 인증 없이 접근 가능합니다.
                        .requestMatchers("/api/owners/signup", "/api/owners/login").permitAll()
                        // 그 외의 모든 사장님 API는 OWNER 권한 필요
                        .requestMatchers("/api/owners/**").hasRole("OWNER")

                        .anyRequest().authenticated() // 나머지는 다 인증 필요
                )
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 끼워 넣음
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
