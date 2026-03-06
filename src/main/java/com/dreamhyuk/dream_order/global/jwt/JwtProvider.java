package com.dreamhyuk.dream_order.global.jwt;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import com.dreamhyuk.dream_order.global.exception.BusinessException;
import com.dreamhyuk.dream_order.global.exception.ErrorCode;
import com.dreamhyuk.dream_order.global.exception.InvalidTokenException;
import com.dreamhyuk.dream_order.global.exception.TokenExpiredException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;
    private final long accessTokenExpiration = 1000L * 60 * 30; //30분
    private final long refreshTokenExpiration = 1000L * 60 * 60 * 24 * 7; //7일

    @PostConstruct
    protected void init() {
        // 보안을 위해 secretKey를 Base64로 인코딩하여 Key 객체 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    //Access Token 생성
    public String createAccessToken(Long id, MemberRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role.name())
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration)) // 30분
                .signWith(key) // 알고리즘은 key 타입을 보고 자동 결정됨
                .compact();
    }

    //Refresh Token 생성
    public String createRefreshToken(Long id, MemberRole role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(id.toString())
                .claim("role", role.name())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration)) // 7일
                .signWith(key) // 알고리즘은 key 타입을 보고 자동 결정됨
                .compact();
    }

    /**
     * 검증 로직: 파싱된 Claims를 반환하거나 예외를 던짐
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰 만료: {}", e.getMessage());
            throw new TokenExpiredException();
        } catch (JwtException e) {
            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }

    /**
     * 필터에서 단순히 유효성만 체크하고 싶을 때 (Optional)
     */
    //단순히 서명이 맞는지, 만료되지 않았는지만 체크 (공통)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false; // 필터에서 다음 체인으로 넘기거나 거부할 용도
        }
    }

    //리프레시 토큰 전용 검증
    public void validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 토큰 내부에 저장된 'type' 클레임이 'REFRESH'인지 확인하는 로직이 있으면 최고입니다.
             String type = claims.get("type", String.class);
             if (!"REFRESH".equals(type)) {
                 throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
             }
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN); // 401: 다시 로그인 유도
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN); // 401: 위조된 토큰
        }
    }



    //토큰에서 사용자 ID 추출
    public Long getMemberIdFromToken(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.valueOf(subject);
    }

    //토큰에서 권한(Role) 추출
    //반환타입을 MemberRole or String 고민 좀 해봐야 함
    public MemberRole getRoleFromToken(String token) {
        String roleName = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);

        return MemberRole.valueOf(roleName); // String을 다시 Enum으로 변환
    }

}
