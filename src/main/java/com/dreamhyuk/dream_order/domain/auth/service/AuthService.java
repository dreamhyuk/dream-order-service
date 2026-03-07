package com.dreamhyuk.dream_order.domain.auth.service;

import com.dreamhyuk.dream_order.domain.auth.dto.AuthRequestDto;
import com.dreamhyuk.dream_order.domain.auth.dto.AuthResponseDto;
import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.domain.member.customer.Customer;
import com.dreamhyuk.dream_order.domain.member.customer.CustomerRepository;
import com.dreamhyuk.dream_order.domain.member.owner.Owner;
import com.dreamhyuk.dream_order.domain.member.owner.OwnerRepository;
import com.dreamhyuk.dream_order.global.jwt.JwtProvider;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final CustomerRepository customerRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    /** 로그인 */
    //Customer 로그인
    @Transactional
    public AuthResponseDto.Token loginCustomer(AuthRequestDto.Login request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return issueTokens(customer.getId(), MemberRole.CUSTOMER);
    }

    //Owner 로그인
    @Transactional
    public AuthResponseDto.Token loginOwner(AuthRequestDto.Login request) {
        Owner owner = ownerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        return issueTokens(owner.getId(), MemberRole.OWNER);
    }

    /** 로그아웃 */
    @Transactional
    public void logout(Long memberId, MemberRole role) {
        String redisKey = "RT:" + role + ":" + memberId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.delete(redisKey);
        } else {
            //이미 로그아웃 되었거나 토큰이 없는 경우
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

    }

    /** 토큰 재발급 */
    @Transactional
    public AuthResponseDto.Token refresh(String refreshToken) {
        // 1. 검증 및 추출
        jwtProvider.validateRefreshToken(refreshToken);
        Long memberId = jwtProvider.getMemberIdFromToken(refreshToken);
        MemberRole role = jwtProvider.getRoleFromToken(refreshToken);

        // 2. Redis 대조
        String redisKey = "RT:" + role + ":" + memberId;
        String savedToken = redisTemplate.opsForValue().get(redisKey);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            // 비정상적인 접근으로 간주하고 모두 삭제 (보안 강화)
            redisTemplate.delete(redisKey);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. 새로운 토큰 발급 및 Redis 갱신 (RTR)
        // access token 을 재발급 받을 때 refresh token 도 갱신
        return issueTokens(memberId, role);
    }

    /**
     * AuthService 내부 메서드
     */
    public AuthResponseDto.Token issueTokens(Long memberId, MemberRole role) {
        // 1. 토큰 발급
        String accessToken = jwtProvider.createAccessToken(memberId, role);
        String refreshToken = jwtProvider.createRefreshToken(memberId, role);

        // 2. Redis 저장
        String redisKey = "RT:" + role + ":" + memberId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, Duration.ofDays(14));

        return AuthResponseDto.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType("Bearer")
                .build();
    }
}
