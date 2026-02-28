package com.example.domain.social.google.support;

import com.example.domain.social.google.payload.response.GoogleTokenResponse;
import com.example.global.exception.SocialException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleIdTokenNonceValidator {

    private final ObjectMapper objectMapper;

    /**
     * nonce 검증
     * <p>
     * - /api/social/google/login에서 생성한 nonce 값과 ID Token의 nonce 클레임이 일치해야 합니다.
     * - ID Token이 없거나 형식이 올바르지 않으면 실패로 처리합니다.
     */
    public void validate(GoogleTokenResponse tokenResponse, String expectedNonce) {
        if (!StringUtils.hasText(expectedNonce)) {
            return;
        }

        final String idToken = tokenResponse != null ? tokenResponse.idToken() : null;
        if (!StringUtils.hasText(idToken)) {
            throw new SocialException(
                    ErrorCode.GOOGLE_OAUTH_ID_TOKEN_MISSING,
                    "구글 id_token이 비어있어 nonce 검증을 수행할 수 없습니다. (scope에 openid가 포함되어야 합니다.)"
            );
        }

        final String nonceFromToken = extractNonceFromIdToken(idToken)
                .orElseThrow(() -> new SocialException(
                        ErrorCode.GOOGLE_OAUTH_INVALID_ID_TOKEN,
                        "id_token에 nonce가 존재하지 않습니다."
                ));

        if (!expectedNonce.equals(nonceFromToken)) {
            throw new SocialException(ErrorCode.GOOGLE_OAUTH_NONCE_MISMATCH, "nonce 불일치");
        }
    }

    private Optional<String> extractNonceFromIdToken(String idToken) {
        try {
            final String[] parts = idToken.split("\\.");
            if (parts.length < 2) {
                throw new SocialException(ErrorCode.GOOGLE_OAUTH_INVALID_ID_TOKEN, "id_token 형식이 올바르지 않습니다.");
            }

            final String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            final JsonNode payload = objectMapper.readTree(payloadJson);
            final JsonNode nonceNode = payload.get("nonce");
            if (nonceNode == null || nonceNode.isNull() || nonceNode.isMissingNode()) {
                return Optional.empty();
            }
            return Optional.ofNullable(nonceNode.stringValue());
        } catch (SocialException e) {
            logNonceParseFailure(e.getErrorCode(), e, idToken);
            throw e;
        } catch (Exception e) {
            logNonceParseFailure(ErrorCode.GOOGLE_OAUTH_INVALID_ID_TOKEN, e, idToken);
            throw new SocialException(ErrorCode.GOOGLE_OAUTH_INVALID_ID_TOKEN, e);
        }
    }

    private void logNonceParseFailure(ErrorCode errorCode, Exception e, String idToken) {
        log.warn("traceId={}, errorCode={}, exceptionName={}, id_token nonce 파싱 실패: tokenPresent={}, tokenLength={}",
                TraceIdUtils.resolveTraceId(), errorCode, e.getClass().getSimpleName(),
                StringUtils.hasText(idToken), safeTokenLength(idToken), e);
    }

    private int safeTokenLength(String token) {
        return token == null ? 0 : token.length();
    }
}
