package com.example.domain.social.google.service.query;

import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.support.GoogleOauthAuthorizeUrlBuilder;
import com.example.domain.social.google.support.GoogleOauthSessionKeys;
import com.example.global.exception.SocialException;
import com.example.global.exception.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GoogleSocialLoginStartQueryService {

    private final GoogleOauthAuthorizeUrlBuilder googleOauthAuthorizeUrlBuilder;
    private final HttpServletRequest httpServletRequest;

    public String getRedirectUrl() {
        final GoogleOauthSession oauthSession = resolveOauthSession();
        return googleOauthAuthorizeUrlBuilder.buildAuthorizeUrl(oauthSession);
    }

    private GoogleOauthSession resolveOauthSession() {
        if (httpServletRequest == null) {
            throw new SocialException(ErrorCode.GOOGLE_OAUTH_STATE_MISMATCH, "구글 OAuth 세션이 존재하지 않습니다.");
        }

        final HttpSession session = httpServletRequest.getSession(false);
        if (session == null) {
            throw new SocialException(ErrorCode.GOOGLE_OAUTH_STATE_MISMATCH, "구글 OAuth 세션이 존재하지 않습니다.");
        }

        final Object stored = session.getAttribute(GoogleOauthSessionKeys.SESSION_KEY);
        if (stored instanceof GoogleOauthSession oauthSession) {
            return oauthSession;
        }

        throw new SocialException(ErrorCode.GOOGLE_OAUTH_STATE_MISMATCH, "구글 OAuth 세션이 존재하지 않습니다.");
    }
}
