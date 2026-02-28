package com.example.global.security.filter;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.security.filter.support.JsonBodyLoginErrorWriter;
import com.example.global.security.filter.support.JsonBodyLoginRequestParser;
import com.example.global.security.filter.support.JsonBodyLoginRequestValidator;
import com.example.global.security.filter.support.LoginRequestParseResult;
import com.example.global.security.filter.support.LoginRequestValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * JSON 바디 기반 로그인 필터
 *
 * <p>
 * - 파라미터 기반 로그인과 별개로, JSON 바디로 로그인할 수 있는 엔드포인트를 제공합니다.
 * - 요청 DTO(일반: {@link com.example.domain.account.payload.request.AccountUserLoginRequest},
 * 관리자: {@link com.example.domain.account.payload.request.AccountAdminLoginRequest})의
 * Bean Validation 및 {@link com.example.domain.account.validator.LoginAccountValidator}를 적용합니다.
 * - 인증 성공/실패 처리는 기존 CustomAuthSuccessHandler/CustomAuthFailureHandler에 위임합니다.
 * </p>
 */
@Slf4j
public class JsonBodyLoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String REQUEST_ATTRIBUTE_LOGIN_ID = "loginId";

    /**
     * JSON 바디 로그인 요청 파싱/검증/에러 응답 지원 컴포넌트
     */
    private final JsonBodyLoginRequestParser requestParser;
    private final JsonBodyLoginRequestValidator requestValidator;
    private final JsonBodyLoginErrorWriter errorWriter;

    public JsonBodyLoginAuthenticationFilter(
            JsonBodyLoginRequestParser requestParser,
            JsonBodyLoginRequestValidator requestValidator,
            JsonBodyLoginErrorWriter errorWriter
    ) {
        if (requestParser == null) {
            throw new IllegalArgumentException("requestParser는 필수입니다.");
        }
        if (requestValidator == null) {
            throw new IllegalArgumentException("requestValidator는 필수입니다.");
        }
        if (errorWriter == null) {
            throw new IllegalArgumentException("errorWriter는 필수입니다.");
        }

        this.requestParser = requestParser;
        this.requestValidator = requestValidator;
        this.errorWriter = errorWriter;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request == null || response == null) {
            throw new AuthenticationServiceException("요청 값이 올바르지 않습니다.");
        }

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("지원하지 않는 인증 방식입니다: " + request.getMethod());
        }

        if (!requestParser.isJsonRequest(request)) {
            errorWriter.writeBadRequest(
                    request,
                    response,
                    ErrorCode.INVALID_PARAMETER,
                    List.of(ApiErrorDetail.of("Content-Type", "application/json 요청만 허용됩니다."))
            );
            return null;
        }

        final LoginRequestParseResult parseResult = requestParser.parse(request);
        if (parseResult.hasErrors()) {
            errorWriter.writeBadRequest(request, response, ErrorCode.INVALID_PARAMETER, parseResult.errors());
            return null;
        }

        final Object loginRequest = parseResult.loginRequest();
        final LoginRequestValidationResult validationResult = requestValidator.validate(loginRequest);
        final String loginId = validationResult.loginId();
        final String password = validationResult.password();

        // 로그인 실패 로그에서 loginId 식별이 가능하도록 attribute에 보관합니다.
        if (loginId != null) {
            request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_ID, loginId);
        }

        if (validationResult.hasErrors()) {
            errorWriter.writeBadRequest(request, response, ErrorCode.INPUT_VALUE_INVALID, validationResult.errors());
            return null;
        }

        final UsernamePasswordAuthenticationToken authRequest = UsernamePasswordAuthenticationToken.unauthenticated(
                loginId,
                password
        );
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
