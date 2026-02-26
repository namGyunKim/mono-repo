package com.example.domain.social.google.support;

import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.global.exception.SocialException;
import com.example.global.exception.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleOauthSessionResolver {

    private final HttpServletRequest httpServletRequest;

    public GoogleOauthSession resolveAndConsume(GoogleSocialRedirectCommand redirectCommand) {
        if (redirectCommand == null) {
            throw new SocialException(ErrorCode.GOOGLE_OAUTH_STATE_MISMATCH, "Google OAuth state/세션 검증 실패");
        }

        if (StringUtils.hasText(redirectCommand.error())) {
            clearOauthSession();
            throw new SocialException(
                    ErrorCode.SOCIAL_TOKEN_ERROR,
                    "구글 OAuth 오류 응답: error=%s, description=%s".formatted(
                            redirectCommand.error(),
                            redirectCommand.errorDescription()
                    )
            );
        }

        return consumeOauthSession(redirectCommand.state())
                .orElseThrow(() -> new SocialException(
                        ErrorCode.GOOGLE_OAUTH_STATE_MISMATCH,
                        "Google OAuth state/세션 검증 실패"
                ));
    }

    private Optional<GoogleOauthSession> consumeOauthSession(String receivedState) {
        if (httpServletRequest == null || !StringUtils.hasText(receivedState)) {
            return Optional.empty();
        }

        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            return Optional.empty();
        }

        Object stored = session.getAttribute(GoogleOauthSessionKeys.SESSION_KEY);
        session.removeAttribute(GoogleOauthSessionKeys.SESSION_KEY);

        if (!(stored instanceof GoogleOauthSession oauthSession)) {
            return Optional.empty();
        }

        if (!StringUtils.hasText(oauthSession.state())) {
            return Optional.empty();
        }

        if (!oauthSession.state().equals(receivedState)) {
            return Optional.empty();
        }
        return Optional.of(oauthSession);
    }

    private void clearOauthSession() {
        if (httpServletRequest == null) {
            return;
        }

        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            return;
        }

        session.removeAttribute(GoogleOauthSessionKeys.SESSION_KEY);
    }
}
