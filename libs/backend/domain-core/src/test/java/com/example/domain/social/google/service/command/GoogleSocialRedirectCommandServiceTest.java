package com.example.domain.social.google.service.command;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.account.payload.response.LoginMemberResponse;
import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.support.GoogleOauthSessionResolver;
import com.example.domain.social.support.SocialLoginTokenPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleSocialRedirectCommandServiceTest {

    @InjectMocks
    private GoogleSocialRedirectCommandService googleSocialRedirectCommandService;

    @Mock
    private GoogleOauthSessionResolver googleOauthSessionResolver;

    @Mock
    private GoogleSocialCommandService googleSocialCommandService;

    @Mock
    private SocialLoginTokenPort socialLoginTokenPort;

    @Test
    @DisplayName("loginByRedirect는 세션 검증 후 소셜 로그인 처리하고 토큰을 발급한다")
    void loginByRedirect_delegates_to_resolver_and_issues_tokens() {
        // Arrange
        final GoogleSocialRedirectCommand command = GoogleSocialRedirectCommand.of(
                "auth-code", "state-value", null, null
        );
        final GoogleOauthSession oauthSession = GoogleOauthSession.of(
                "state-value", "nonce", "codeVerifier", "codeChallenge", System.currentTimeMillis()
        );
        final Long memberId = 42L;
        final LoginTokenResponse expectedResponse = LoginTokenResponse.of(null, "access-token", "refresh-token");

        given(googleOauthSessionResolver.resolveAndConsume(command)).willReturn(oauthSession);
        given(googleSocialCommandService.registerOrLoginBySocialCode(any(GoogleOauthLoginCommand.class)))
                .willReturn(memberId);
        given(socialLoginTokenPort.issueTokens(memberId)).willReturn(expectedResponse);

        // Act
        final LoginTokenResponse result = googleSocialRedirectCommandService.loginByRedirect(command);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        verify(googleOauthSessionResolver).resolveAndConsume(command);
        verify(googleSocialCommandService).registerOrLoginBySocialCode(any(GoogleOauthLoginCommand.class));
        verify(socialLoginTokenPort).issueTokens(memberId);
    }
}
