package com.example.domain.social.google.payload.dto;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 구글 OAuth 토큰 교환 요청 DTO
 */
public record GoogleTokenRequestCommand(
        String grantType,
        String clientId,
        String clientSecret,
        String redirectUri,
        String code,
        String codeVerifier
) {
    private static final String AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code";

    public static GoogleTokenRequestCommand of(
            String grantType,
            String clientId,
            String clientSecret,
            String redirectUri,
            String code,
            String codeVerifier
    ) {
        return new GoogleTokenRequestCommand(grantType, clientId, clientSecret, redirectUri, code, codeVerifier);
    }

    public static GoogleTokenRequestCommand ofAuthorizationCode(
            String clientId,
            String clientSecret,
            String redirectUri,
            String code,
            String codeVerifier
    ) {
        return of(AUTHORIZATION_CODE_GRANT_TYPE, clientId, clientSecret, redirectUri, code, codeVerifier);
    }

    public MultiValueMap<String, String> toFormData() {
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", grantType);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        if (StringUtils.hasText(codeVerifier)) {
            formData.add("code_verifier", codeVerifier);
        }
        return formData;
    }
}
