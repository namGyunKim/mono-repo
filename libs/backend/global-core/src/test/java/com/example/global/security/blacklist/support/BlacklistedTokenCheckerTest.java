package com.example.global.security.blacklist.support;

import com.example.global.security.blacklist.BlacklistedTokenRepository;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenHashQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BlacklistedTokenCheckerTest {

    @InjectMocks
    private BlacklistedTokenChecker blacklistedTokenChecker;

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Test
    @DisplayName("빈 문자열 토큰은 블랙리스트 확인 없이 false를 반환한다")
    void isBlacklisted_blank_returns_false() {
        // Arrange
        final String blankToken = "   ";

        // Act
        final boolean result = blacklistedTokenChecker.isBlacklisted(blankToken);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenRepository, never()).existsByTokenHash(any());
    }

    @Test
    @DisplayName("null 토큰은 블랙리스트 확인 없이 false를 반환한다")
    void isBlacklisted_null_returns_false() {
        // Arrange / Act
        final boolean result = blacklistedTokenChecker.isBlacklisted(null);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenRepository, never()).existsByTokenHash(any());
    }

    @Test
    @DisplayName("유효한 토큰이 블랙리스트에 존재하면 true를 반환한다")
    void isBlacklisted_valid_token_exists_returns_true() {
        // Arrange
        final String token = "valid-access-token";
        given(blacklistedTokenRepository.existsByTokenHash(any(BlacklistedTokenHashQuery.class)))
                .willReturn(true);

        // Act
        final boolean result = blacklistedTokenChecker.isBlacklisted(token);

        // Assert
        assertThat(result).isTrue();
        verify(blacklistedTokenRepository).existsByTokenHash(any(BlacklistedTokenHashQuery.class));
    }

    @Test
    @DisplayName("유효한 토큰이 블랙리스트에 없으면 false를 반환한다")
    void isBlacklisted_valid_token_not_exists_returns_false() {
        // Arrange
        final String token = "valid-access-token";
        given(blacklistedTokenRepository.existsByTokenHash(any(BlacklistedTokenHashQuery.class)))
                .willReturn(false);

        // Act
        final boolean result = blacklistedTokenChecker.isBlacklisted(token);

        // Assert
        assertThat(result).isFalse();
        verify(blacklistedTokenRepository).existsByTokenHash(any(BlacklistedTokenHashQuery.class));
    }
}
