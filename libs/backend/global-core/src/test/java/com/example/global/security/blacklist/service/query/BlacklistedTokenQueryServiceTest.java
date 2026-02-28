package com.example.global.security.blacklist.service.query;

import com.example.global.security.blacklist.payload.dto.BlacklistedTokenCheckQuery;
import com.example.global.security.blacklist.support.BlacklistedTokenChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlacklistedTokenQueryServiceTest {

    @InjectMocks
    private BlacklistedTokenQueryService blacklistedTokenQueryService;

    @Mock
    private BlacklistedTokenChecker blacklistedTokenChecker;

    @Test
    @DisplayName("null query는 블랙리스트 확인 없이 false를 반환한다")
    void isBlacklisted_null_query_returns_false() {
        // Arrange / Act
        final boolean result = blacklistedTokenQueryService.isBlacklisted(null);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenChecker, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("빈 토큰을 가진 query는 블랙리스트 확인 없이 false를 반환한다")
    void isBlacklisted_blank_token_returns_false() {
        // Arrange
        final BlacklistedTokenCheckQuery query = BlacklistedTokenCheckQuery.of("   ");

        // Act
        final boolean result = blacklistedTokenQueryService.isBlacklisted(query);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenChecker, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("유효한 토큰 query는 checker에 위임하여 true를 반환한다")
    void isBlacklisted_valid_token_delegates_returns_true() {
        // Arrange
        final String token = "valid-token";
        final BlacklistedTokenCheckQuery query = BlacklistedTokenCheckQuery.of(token);
        given(blacklistedTokenChecker.isBlacklisted(token)).willReturn(true);

        // Act
        final boolean result = blacklistedTokenQueryService.isBlacklisted(query);

        // Assert
        assertThat(result).isTrue();
        verify(blacklistedTokenChecker).isBlacklisted(token);
    }

    @Test
    @DisplayName("유효한 토큰 query는 checker에 위임하여 false를 반환한다")
    void isBlacklisted_valid_token_delegates_returns_false() {
        // Arrange
        final String token = "valid-token";
        final BlacklistedTokenCheckQuery query = BlacklistedTokenCheckQuery.of(token);
        given(blacklistedTokenChecker.isBlacklisted(token)).willReturn(false);

        // Act
        final boolean result = blacklistedTokenQueryService.isBlacklisted(query);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenChecker).isBlacklisted(token);
    }
}
