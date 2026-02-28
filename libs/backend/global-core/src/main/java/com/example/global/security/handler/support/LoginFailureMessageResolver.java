package com.example.global.security.handler.support;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureMessageResolver {

    public String resolve(AuthenticationException exception) {
        if (exception == null) {
            return "로그인 실패";
        }

        return switch (exception) {
            case BadCredentialsException _ -> "비밀번호 불일치";
            case UsernameNotFoundException _ -> "계정 없음";
            case InternalAuthenticationServiceException _ -> "내부 시스템 에러";
            case LockedException _ -> "계정 잠김";
            case DisabledException _ -> "계정 비활성화";
            case AccountExpiredException _ -> "계정 만료";
            case CredentialsExpiredException _ -> "비밀번호 만료";
            default -> "로그인 실패(" + exception.getClass().getSimpleName() + ")";
        };
    }
}
