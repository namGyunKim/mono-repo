package com.example.global.security.handler;

import com.example.domain.member.payload.dto.MemberLoginIdQuery;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.security.handler.support.LoginFailureEventPublisher;
import com.example.global.security.handler.support.LoginFailureLogWriter;
import com.example.global.security.handler.support.LoginFailureMessageResolver;
import com.example.global.security.handler.support.LoginFailureRequestResolver;
import com.example.global.security.handler.support.LoginFailureResponseWriter;
import com.example.global.security.service.query.MemberAuthQueryService;
import com.example.global.utils.LoginLoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 로그인 실패 핸들러
 * - 로그인 실패 시 로그 이벤트를 발행하고 JSON 실패 응답을 반환합니다.
 *
 * <p>
 * 정책 정리:
 * - 입력값 누락(폼 로그인) -> 400 + INPUT_VALUE_INVALID (+ errors[])
 * - 인증 실패(아이디/비밀번호 불일치 등) -> 401 + AUTHENTICATION_FAILED
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final MemberAuthQueryService memberAuthQueryService;
    private final LoginFailureRequestResolver loginFailureRequestResolver;
    private final LoginFailureMessageResolver loginFailureMessageResolver;
    private final LoginFailureEventPublisher loginFailureEventPublisher;
    private final LoginFailureLogWriter loginFailureLogWriter;
    private final LoginFailureResponseWriter loginFailureResponseWriter;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        final String loginIdForEvent = loginFailureRequestResolver.resolveLoginIdOrDefault(request, LoginLoggingUtils.DEFAULT_UNKNOWN_LOGIN_ID);

        final List<ApiErrorDetail> missingCredentialErrors = loginFailureRequestResolver.resolveMissingCredentialErrors(request);
        if (!missingCredentialErrors.isEmpty()) {
            handleMissingCredentials(request, response, loginIdForEvent, missingCredentialErrors);
            return;
        }

        handleAuthFailure(request, response, exception, loginIdForEvent);
    }

    private void handleMissingCredentials(
            HttpServletRequest request, HttpServletResponse response,
            String loginIdForEvent, List<ApiErrorDetail> errors
    ) throws IOException {
        loginFailureEventPublisher.publishLoginFailEvent(loginIdForEvent, null, "로그인 요청 값 누락");
        markFilterLogged(request);
        loginFailureLogWriter.logMissingCredentials(request, loginIdForEvent, errors);
        loginFailureResponseWriter.writeErrorResponse(response, HttpStatus.BAD_REQUEST, ErrorCode.INPUT_VALUE_INVALID, errors);
    }

    private void handleAuthFailure(
            HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception, String loginIdForEvent
    ) throws IOException {
        final String loginId = loginFailureRequestResolver.resolveLoginId(request).orElse(null);
        final Long memberId = (loginId != null && !loginId.isBlank())
                ? memberAuthQueryService.findMemberIdByLoginId(MemberLoginIdQuery.of(loginId)).orElse(null)
                : null;

        final String detailMessage = loginFailureMessageResolver.resolve(exception);
        loginFailureEventPublisher.publishLoginFailEvent(loginIdForEvent, memberId, detailMessage);
        markFilterLogged(request);
        loginFailureLogWriter.logAuthFailure(request, loginId, detailMessage);
        loginFailureResponseWriter.writeErrorResponse(response, HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_FAILED, List.of());
    }

    private void markFilterLogged(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
    }
}
