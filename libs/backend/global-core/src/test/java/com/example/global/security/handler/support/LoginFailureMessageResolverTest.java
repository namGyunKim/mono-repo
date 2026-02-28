package com.example.global.security.handler.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class LoginFailureMessageResolverTest {

    private final LoginFailureMessageResolver resolver = new LoginFailureMessageResolver();

    @Test
    void resolve_null_returns_default() {
        assertThat(resolver.resolve(null)).isEqualTo("로그인 실패");
    }

    @Test
    void resolve_BadCredentialsException() {
        assertThat(resolver.resolve(new BadCredentialsException("bad"))).isEqualTo("비밀번호 불일치");
    }

    @Test
    void resolve_UsernameNotFoundException() {
        assertThat(resolver.resolve(new UsernameNotFoundException("not found"))).isEqualTo("계정 없음");
    }

    @Test
    void resolve_InternalAuthenticationServiceException() {
        assertThat(resolver.resolve(new InternalAuthenticationServiceException("internal"))).isEqualTo("내부 시스템 에러");
    }

    @Test
    void resolve_LockedException() {
        assertThat(resolver.resolve(new LockedException("locked"))).isEqualTo("계정 잠김");
    }

    @Test
    void resolve_DisabledException() {
        assertThat(resolver.resolve(new DisabledException("disabled"))).isEqualTo("계정 비활성화");
    }

    @Test
    void resolve_AccountExpiredException() {
        assertThat(resolver.resolve(new AccountExpiredException("expired"))).isEqualTo("계정 만료");
    }

    @Test
    void resolve_CredentialsExpiredException() {
        assertThat(resolver.resolve(new CredentialsExpiredException("cred expired"))).isEqualTo("비밀번호 만료");
    }
}
