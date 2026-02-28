package com.example.global.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuthPkceUtilsTest {

    @Test
    void createState_returns_non_blank() {
        final String state = OAuthPkceUtils.createState();
        assertThat(state).isNotBlank();
    }

    @Test
    void createState_different_calls_produce_different_values() {
        final String state1 = OAuthPkceUtils.createState();
        final String state2 = OAuthPkceUtils.createState();
        assertThat(state1).isNotEqualTo(state2);
    }

    @Test
    void createNonce_returns_non_blank() {
        final String nonce = OAuthPkceUtils.createNonce();
        assertThat(nonce).isNotBlank();
    }

    @Test
    void createCodeVerifier_length_at_least_43() {
        final String verifier = OAuthPkceUtils.createCodeVerifier();
        assertThat(verifier.length()).isGreaterThanOrEqualTo(43);
    }

    @Test
    void createCodeVerifier_uses_url_safe_characters() {
        final String verifier = OAuthPkceUtils.createCodeVerifier();
        assertThat(verifier).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void createCodeChallengeS256_returns_non_blank_for_valid_verifier() {
        final String verifier = OAuthPkceUtils.createCodeVerifier();
        final String challenge = OAuthPkceUtils.createCodeChallengeS256(verifier);
        assertThat(challenge).isNotBlank();
    }

    @Test
    void createCodeChallengeS256_null_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> OAuthPkceUtils.createCodeChallengeS256(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createCodeChallengeS256_blank_throws_IllegalArgumentException() {
        assertThatThrownBy(() -> OAuthPkceUtils.createCodeChallengeS256("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createCodeChallengeS256_same_verifier_produces_same_challenge() {
        final String verifier = OAuthPkceUtils.createCodeVerifier();
        final String challenge1 = OAuthPkceUtils.createCodeChallengeS256(verifier);
        final String challenge2 = OAuthPkceUtils.createCodeChallengeS256(verifier);
        assertThat(challenge1).isEqualTo(challenge2);
    }

    @Test
    void createCodeChallengeS256_different_verifiers_produce_different_challenges() {
        final String verifier1 = OAuthPkceUtils.createCodeVerifier();
        final String verifier2 = OAuthPkceUtils.createCodeVerifier();
        final String challenge1 = OAuthPkceUtils.createCodeChallengeS256(verifier1);
        final String challenge2 = OAuthPkceUtils.createCodeChallengeS256(verifier2);
        assertThat(challenge1).isNotEqualTo(challenge2);
    }
}
