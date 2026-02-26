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

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOauthTokenRevoker {

    private final GoogleOauthClient googleOauthClient;
    private final RefreshTokenCrypto refreshTokenCrypto;

    public void revoke(SocialAccount socialAccount, Long memberId, String loginId) {
        String refreshTokenEncrypted = socialAccount != null ? socialAccount.getRefreshTokenEncrypted() : null;
        if (!StringUtils.hasText(refreshTokenEncrypted)) {
            log.info(
                    "traceId={}, 구글 토큰 revoke 스킵: refresh_token_encrypted 없음 memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
            return;
        }

        String refreshToken = refreshTokenCrypto.decrypt(refreshTokenEncrypted);
        if (!StringUtils.hasText(refreshToken)) {
            log.warn(
                    "traceId={}, 구글 토큰 revoke 스킵: refresh_token 복호화 실패 memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
            return;
        }

        long revokeStartNanos = System.nanoTime();
        try {
            ResponseEntity<Void> response = googleOauthClient.revokeToken(refreshToken);
            long elapsedMs = elapsedMs(revokeStartNanos);
            log.info(
                    "traceId={}, 구글 토큰 revoke 호출 완료: status={}, elapsedMs={}, memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    response.getStatusCode().value(),
                    elapsedMs,
                    memberId,
                    loginId
            );
        } catch (RestClientResponseException e) {
            long elapsedMs = elapsedMs(revokeStartNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 revoke 호출 실패: status={}, elapsedMs={}, memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_UNLINK_ERROR,
                    e.getClass().getSimpleName(),
                    e.getStatusCode().value(),
                    elapsedMs,
                    memberId,
                    loginId,
                    e
            );
        } catch (RestClientException e) {
            long elapsedMs = elapsedMs(revokeStartNanos);
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 revoke 호출 실패: status=UNKNOWN, elapsedMs={}, memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_UNLINK_ERROR,
                    e.getClass().getSimpleName(),
                    elapsedMs,
                    memberId,
                    loginId,
                    e
            );
        } catch (Exception e) {
            log.warn(
                    "traceId={}, errorCode={}, exceptionName={}, 구글 토큰 revoke 실패: memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.GOOGLE_API_UNLINK_ERROR,
                    e.getClass().getSimpleName(),
                    memberId,
                    loginId,
                    e
            );
        }
    }

    private long elapsedMs(long startNanos) {
        if (startNanos <= 0) {
            return 0L;
        }
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }
}
