package com.example.domain.social.google.service.command;

import com.example.domain.social.google.support.GoogleOauthSessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GoogleSocialLoginStartCommandServiceTest {

    @InjectMocks
    private GoogleSocialLoginStartCommandService googleSocialLoginStartCommandService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @Test
    @DisplayName("prepareLoginSession은 세션을 생성하고 OAuth 세션 속성을 저장한다")
    void prepareLoginSession_stores_session_attribute() {
        // Arrange
        given(httpServletRequest.getSession(true)).willReturn(httpSession);

        // Act
        googleSocialLoginStartCommandService.prepareLoginSession();

        // Assert
        final ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        verify(httpSession).setAttribute(keyCaptor.capture(), valueCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo(GoogleOauthSessionKeys.SESSION_KEY);
        assertThat(valueCaptor.getValue()).isNotNull();
    }
}
