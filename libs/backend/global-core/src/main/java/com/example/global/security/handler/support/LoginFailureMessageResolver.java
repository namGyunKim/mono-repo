package com.example.global.security.handler.support;

import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureMessageResolver {

    public String resolve(AuthenticationException exception) {
        if (exception == null) {
            return "로그인 실패";
        }
        if (exception instanceof BadCredentialsException) {
            return "비밀번호 불일치";
        }
        if (exception instanceof UsernameNotFoundException) {
            return "계정 없음";
        }
        if (exception instanceof InternalAuthenticationServiceException) {
            return "내부 시스템 에러";
        }
        if (exception instanceof LockedException) {
            return "계정 잠김";
        }
        if (exception instanceof DisabledException) {
            return "계정 비활성화";
        }
        if (exception instanceof AccountExpiredException) {
            return "계정 만료";
        }
        if (exception instanceof CredentialsExpiredException) {
            return "비밀번호 만료";
        }
        return "로그인 실패(" + exception.getClass().getSimpleName() + ")";
    }
}
