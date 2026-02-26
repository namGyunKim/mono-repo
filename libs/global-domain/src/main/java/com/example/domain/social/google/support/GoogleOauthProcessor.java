package com.example.domain.social.google.support;

import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthResult;
import com.example.domain.social.google.payload.dto.GoogleTokenRequestCommand;
import com.example.domain.social.google.payload.response.GoogleTokenResponse;
import com.example.domain.social.google.payload.response.GoogleUserInfoResponse;
import com.example.global.config.social.GoogleApiClient;
import com.example.global.exception.SocialException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.TimeUnit;

/**
 * 구글 OAuth 인증 플로우 처리 전용 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOauthProcessor {

    private final GoogleApiClient googleApiClient;
    private final GoogleIdTokenNonceValidator idTokenNonceValidator;

    @Value("${social.google.secretKey}")
    private String clientSecret;
    @Value("${social.google.clientId}")
    private String clientId;
    @Value("${social.google.redirectUri}")
    private String redirectUri;

    public GoogleOauthResult authenticate(GoogleOauthLoginCommand command) {
        String code = requireAuthorizationCode(command);
        String codeVerifier = command != null ? command.codeVerifier() : null;
        String expectedNonce = command != null ? command.expectedNonce() : null;

        GoogleTokenResponse tokenResponse = requireAccessToken(requestAccessToken(code, codeVerifier));
        idTokenNonceValidator.validate(tokenResponse, expectedNonce);

        GoogleUserInfoResponse userInfo = requestUserInfo(tokenResponse.accessToken());
        return GoogleOauthResult.of(userInfo, tokenResponse.refreshToken());
    }

    private GoogleTokenResponse requestAccessToken(String code, String codeVerifier) {
        long startNanos = System.nanoTime();
        GoogleTokenRequestCommand tokenRequest = GoogleTokenRequestCommand.ofAuthorizationCode(
                clientId,
                clientSecret,
                redirectUri,
                code,
                codeVerifier
        );
        try {
            ResponseEntity<GoogleTokenResponse> response = googleApiClient.getToken(tokenRequest.toFormData());
            long elapsedMs = elapsedMs(startNanos);
            GoogleTokenResponse body = response.getBody();
            if (body == null) {
                log.warn(
                        "traceId={}, 구글 토큰 API 응답 바디가 비어있습니다: status={}, elapsedMs={}",
                        TraceIdUtils.resolveTraceId(),
                        response.getStatusCode().value(),
                        elapsedMs
                );
                return null;
            }
            log.info(
                    "traceId={}, 구글 토큰 API 호출 완료: status={}, elapsedMs={}",
                    TraceIdUtils.resolveTraceId(),
                    response.getStatusCode().value(),
                    elapsedMs
            );
            return body;
        } catch (RestClientResponseException e) {
            long elapsedMs = elapsedMs(startNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 API 호출 실패: status={}, elapsedMs={}, codePresent={}, codeVerifierPresent={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_GET_TOKEN_ERROR,
                    e.getClass().getSimpleName(),
                    e.getStatusCode().value(),
                    elapsedMs,
                    StringUtils.hasText(code),
                    StringUtils.hasText(codeVerifier),
                    e
            );
            throw new SocialException(ErrorCode.GOOGLE_API_GET_TOKEN_ERROR, e);
        } catch (RestClientException e) {
            long elapsedMs = elapsedMs(startNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 API 호출 실패: status=UNKNOWN, elapsedMs={}, codePresent={}, codeVerifierPresent={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_GET_TOKEN_ERROR,
                    e.getClass().getSimpleName(),
                    elapsedMs,
                    StringUtils.hasText(code),
                    StringUtils.hasText(codeVerifier),
                    e
            );
            throw new SocialException(ErrorCode.GOOGLE_API_GET_TOKEN_ERROR, e);
        }
    }

    private String requireAuthorizationCode(GoogleOauthLoginCommand command) {
        String code = command != null ? command.code() : null;
        if (!StringUtils.hasText(code)) {
            throw new SocialException(ErrorCode.GOOGLE_API_GET_CODE_ERROR, "구글 OAuth code가 누락되었습니다.");
        }
        return code;
    }

    private GoogleTokenResponse requireAccessToken(GoogleTokenResponse tokenResponse) {
        if (tokenResponse == null) {
            throw new SocialException(ErrorCode.GOOGLE_API_GET_TOKEN_ERROR, "구글 토큰 응답이 비어있습니다.");
        }

        if (!StringUtils.hasText(tokenResponse.accessToken())) {
            throw new SocialException(ErrorCode.GOOGLE_API_GET_TOKEN_ERROR, "구글 access_token이 비어있습니다.");
        }

        return tokenResponse;
    }

    private GoogleUserInfoResponse requestUserInfo(String accessToken) {
        long startNanos = System.nanoTime();
        try {
            ResponseEntity<GoogleUserInfoResponse> response = googleApiClient.getUserInfo(accessToken);
            long elapsedMs = elapsedMs(startNanos);
            GoogleUserInfoResponse body = response.getBody();
            if (body == null) {
                log.warn(
                        "traceId={}, 구글 사용자 정보 API 응답 바디가 비어있습니다: status={}, elapsedMs={}, accessTokenPresent={}",
                        TraceIdUtils.resolveTraceId(),
                        response.getStatusCode().value(),
                        elapsedMs,
                        StringUtils.hasText(accessToken)
                );
                throw new SocialException(ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR, "구글 사용자 정보 응답이 비어있습니다.");
            }
            log.info(
                    "traceId={}, 구글 사용자 정보 API 호출 완료: status={}, elapsedMs={}",
                    TraceIdUtils.resolveTraceId(),
                    response.getStatusCode().value(),
                    elapsedMs
            );
            return body;
        } catch (RestClientResponseException e) {
            long elapsedMs = elapsedMs(startNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 사용자 정보 API 호출 실패: status={}, elapsedMs={}, accessTokenPresent={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR,
                    e.getClass().getSimpleName(),
                    e.getStatusCode().value(),
                    elapsedMs,
                    StringUtils.hasText(accessToken),
                    e
            );
            throw new SocialException(ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR, e);
        } catch (RestClientException e) {
            long elapsedMs = elapsedMs(startNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 사용자 정보 API 호출 실패: status=UNKNOWN, elapsedMs={}, accessTokenPresent={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR,
                    e.getClass().getSimpleName(),
                    elapsedMs,
                    StringUtils.hasText(accessToken),
                    e
            );
            throw new SocialException(ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR, e);
        }
    }

    private long elapsedMs(long startNanos) {
        if (startNanos <= 0) {
            return 0L;
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
