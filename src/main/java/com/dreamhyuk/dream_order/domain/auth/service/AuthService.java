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
    @Transactional
    public AuthResponseDto.Token login(AuthRequestDto.Login request) {
        // 1. 타입에 따른 유저 정보 조회 (ID와 암호화된 비밀번호만 추출)
        AuthInfo authInfo = getAuthInfo(request.getEmail(), request.getRole());

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), authInfo.getEncodedPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 3. 토큰 발급 (성공)
        String accessToken = jwtProvider.createAccessToken(authInfo.getId(), request.getRole());
        String refreshToken = jwtProvider.createRefreshToken(authInfo.getId(), request.getRole());

        // 4. Redis에 Refresh Token 저장
        // Key 형식: RT:ROLE:email
        String redisKey = "RT:" + request.getRole() + ":" + authInfo.getId();

        redisTemplate.opsForValue().set(
                redisKey,
                refreshToken,
                Duration.ofDays(14) // 리프레시 토큰 유효 기간과 동일하게 설정
        );

        return AuthResponseDto.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType("Bearer")
                .build();
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

        log.info("입력받은 토큰: {}", refreshToken);
        log.info("저장된 토큰: {}", savedToken);

        if (savedToken == null || !savedToken.equals(refreshToken)) {
            // 비정상적인 접근으로 간주하고 모두 삭제 (보안 강화)
            redisTemplate.delete(redisKey);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. 새로운 토큰 세트 발급 (RTR 방식)
        AuthResponseDto.Token newToken = createTokenSet(memberId, role);

        // 4. Redis 갱신 (기존 토큰 무효화 효과)
        redisTemplate.opsForValue().set(redisKey, newToken.getRefreshToken(), Duration.ofDays(14));

        return newToken;
    }

    /**
     * AuthService 내부 메서드
     */
    private AuthResponseDto.Token createTokenSet(Long memberId, MemberRole role) {
        //JwtProvider 를 통해 각 토큰 생성
        String accessToken = jwtProvider.createAccessToken(memberId, role);
        String refreshToken = jwtProvider.createRefreshToken(memberId, role);

        return AuthResponseDto.Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType("Bearer")
                .build();
    }

    private Long findUserIdByEmail(String email, MemberRole role) {
        if (role == MemberRole.CUSTOMER) {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            return customer.getId();
        } else {
            Owner owner = ownerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            return owner.getId();

        }
    }


    /** 타입별 엔티티 조회 분기 처리 */
    // if-else 분기를 담당하는 헬퍼 메서드
    private AuthInfo getAuthInfo(String email, MemberRole role) {
        if (role == MemberRole.CUSTOMER) {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            return new AuthInfo(customer.getId(), customer.getPassword());
        }

        // OWNER인 경우
        if (role == MemberRole.OWNER) { // else 대신 명확히 체크
            Owner owner = ownerRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            return new AuthInfo(owner.getId(), owner.getPassword());
        }

        throw new BusinessException(ErrorCode.INVALID_TYPE);
    }

    //rider 추가되면 switch 문을 사용하는 게 깔끔할 수도..
/*
    private AuthInfo getAuthInfo(String email, MemberRole role) {
        return switch (role) {
            case CUSTOMER -> customerRepository.findByEmail(email)
                    .map(c -> new AuthInfo(c.getId(), c.getPassword()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            case OWNER -> ownerRepository.findByEmail(email)
                    .map(o -> new AuthInfo(o.getId(), o.getPassword()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            case RIDER -> riderRepository.findByEmail(email)
                    .map(r -> new AuthInfo(r.getId(), r.getPassword()))
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        };
    }
*/

    //Service 에서만 쓰는 객체
    @Getter
    @AllArgsConstructor
    private static class AuthInfo {
        private Long id;
        private String encodedPassword;
    }
}
