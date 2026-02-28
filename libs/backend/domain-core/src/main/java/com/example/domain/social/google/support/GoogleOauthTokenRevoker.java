package com.example.domain.social.google.support;

import com.example.domain.social.entity.SocialAccount;
import com.example.global.config.social.GoogleOauthClient;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.RefreshTokenCrypto;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOauthTokenRevoker {

    private final GoogleOauthClient googleOauthClient;
    private final RefreshTokenCrypto refreshTokenCrypto;

    public void revoke(SocialAccount socialAccount, Long memberId, String loginId) {
        final Optional<String> decryptedToken = resolveDecryptedRefreshToken(socialAccount, memberId, loginId);
        if (decryptedToken.isEmpty()) {
            return;
        }

        final long startNanos = System.nanoTime();
        try {
            final ResponseEntity<Void> response = googleOauthClient.revokeToken(decryptedToken.get());
            log.info(
                    "traceId={}, 구글 토큰 revoke 호출 완료: status={}, elapsedMs={}, memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    response.getStatusCode().value(),
                    GoogleOauthTimingSupport.elapsedMs(startNanos),
                    memberId,
                    loginId
            );
        } catch (RestClientResponseException e) {
            logRevokeFailure("status=" + e.getStatusCode().value(), startNanos, memberId, loginId, e);
        } catch (RestClientException e) {
            logRevokeFailure("status=UNKNOWN", startNanos, memberId, loginId, e);
        } catch (Exception e) {
            logRevokeFailure(null, startNanos, memberId, loginId, e);
        }
    }

    private Optional<String> resolveDecryptedRefreshToken(SocialAccount socialAccount, Long memberId, String loginId) {
        final String refreshTokenEncrypted = socialAccount != null ? socialAccount.getRefreshTokenEncrypted() : null;
        if (!StringUtils.hasText(refreshTokenEncrypted)) {
            log.info(
                    "traceId={}, 구글 토큰 revoke 스킵: refresh_token_encrypted 없음 memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
            return Optional.empty();
        }

        final String refreshToken = refreshTokenCrypto.decrypt(refreshTokenEncrypted);
        if (!StringUtils.hasText(refreshToken)) {
            log.warn(
                    "traceId={}, 구글 토큰 revoke 스킵: refresh_token 복호화 실패 memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
            return Optional.empty();
        }

        return Optional.of(refreshToken);
    }

    private void logRevokeFailure(String statusInfo, long startNanos, Long memberId, String loginId, Exception e) {
        final String message = statusInfo != null
                ? "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 revoke 호출 실패: {}, elapsedMs={}, memberId={}, loginId={}"
                : "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 revoke 실패: memberId={}, loginId={}";

        if (statusInfo != null) {
            log.warn(message,
                    TraceIdUtils.resolveTraceId(), ErrorCode.GOOGLE_API_UNLINK_ERROR, e.getClass().getSimpleName(),
                    statusInfo, GoogleOauthTimingSupport.elapsedMs(startNanos), memberId, loginId, e);
        } else {
            log.warn(message,
                    TraceIdUtils.resolveTraceId(), ErrorCode.GOOGLE_API_UNLINK_ERROR, e.getClass().getSimpleName(),
                    memberId, loginId, e);
        }
    }
}
