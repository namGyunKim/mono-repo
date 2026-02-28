package com.example.domain.social.google.service.query;

import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.support.GoogleOauthAuthorizeUrlBuilder;
import com.example.domain.social.google.support.GoogleOauthSessionKeys;
import com.example.global.exception.SocialException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GoogleSocialLoginStartQueryServiceTest {

    @InjectMocks
    private GoogleSocialLoginStartQueryService googleSocialLoginStartQueryService;

    @Mock
    private GoogleOauthAuthorizeUrlBuilder googleOauthAuthorizeUrlBuilder;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @Test
    @DisplayName("세션이 존재하지 않으면 SocialException이 발생한다")
    void getRedirectUrl_no_session_throws_SocialException() {
        // Arrange
        given(httpServletRequest.getSession(false)).willReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> googleSocialLoginStartQueryService.getRedirectUrl())
                .isInstanceOf(SocialException.class)
                .hasMessageContaining("구글 OAuth 세션이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("세션에 OAuth 속성이 없으면 SocialException이 발생한다")
    void getRedirectUrl_no_session_attribute_throws_SocialException() {
        // Arrange
        given(httpServletRequest.getSession(false)).willReturn(httpSession);
        given(httpSession.getAttribute(GoogleOauthSessionKeys.SESSION_KEY)).willReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> googleSocialLoginStartQueryService.getRedirectUrl())
                .isInstanceOf(SocialException.class)
                .hasMessageContaining("구글 OAuth 세션이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("유효한 OAuth 세션이 존재하면 리다이렉트 URL을 반환한다")
    void getRedirectUrl_valid_session_returns_url() {
        // Arrange
        final GoogleOauthSession oauthSession = GoogleOauthSession.of(
                "state", "nonce", "codeVerifier", "codeChallenge", System.currentTimeMillis()
        );
        final String expectedUrl = "https://accounts.google.com/o/oauth2/v2/auth?client_id=test";

        given(httpServletRequest.getSession(false)).willReturn(httpSession);
        given(httpSession.getAttribute(GoogleOauthSessionKeys.SESSION_KEY)).willReturn(oauthSession);
        given(googleOauthAuthorizeUrlBuilder.buildAuthorizeUrl(oauthSession)).willReturn(expectedUrl);

        // Act
        final String result = googleSocialLoginStartQueryService.getRedirectUrl();

        // Assert
        assertThat(result).isEqualTo(expectedUrl);
    }
}
