package com.dreamhyuk.dream_order.global.userdetails;

import com.dreamhyuk.dream_order.domain.member.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final String username;
    private final MemberRole role;
    private final Collection<? extends GrantedAuthority> authorities;

    public Long getMemberId() {
        return memberId;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // 계정 상태 관련 메서드들도 기본적으로 true를 반환해야 로그인이 유지
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
