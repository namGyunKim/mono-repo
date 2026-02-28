package com.example.domain.social.google.support;

import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class GoogleOauthAuthorizeUrlBuilder {

    @Value("${social.google.baseUrl}")
    private String baseUrl;
    @Value("${social.google.clientId}")
    private String clientId;
    @Value("${social.google.redirectUri}")
    private String redirectUri;
    @Value("${social.google.scope}")
    private String scope;

    public String buildBaseAuthorizeUrl() {
        return baseBuilder().encode().toUriString();
    }

    public String buildAuthorizeUrl(GoogleOauthSession oauthSession) {
        if (oauthSession == null) {
            throw new IllegalArgumentException("oauthSession은 필수입니다.");
        }

        return baseBuilder()
                .queryParam("state", oauthSession.state())
                .queryParam("nonce", oauthSession.nonce())
                .queryParam("code_challenge", oauthSession.codeChallenge())
                .queryParam("code_challenge_method", "S256")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .encode()
                .toUriString();
    }

    private UriComponentsBuilder baseBuilder() {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", normalizeScope(scope));
    }

    private String normalizeScope(String rawScope) {
        final Set<String> parsed = parseRawScope(rawScope);
        final LinkedHashSet<String> scopes = ensureOpenIdFirst(parsed);

        // [기본] userinfo 호출에서 필요한 값
        scopes.add("email");
        scopes.add("profile");

        return String.join(" ", scopes);
    }

    private Set<String> parseRawScope(String rawScope) {
        if (!StringUtils.hasText(rawScope)) {
            return new LinkedHashSet<>();
        }
        final Set<String> scopes = new LinkedHashSet<>();
        final String[] tokens = rawScope.replace(",", " ").trim().split("\\s+");
        for (final String token : tokens) {
            if (StringUtils.hasText(token)) {
                scopes.add(token.trim());
            }
        }
        return scopes;
    }

    private LinkedHashSet<String> ensureOpenIdFirst(Set<String> scopes) {
        if (scopes.contains("openid")) {
            return new LinkedHashSet<>(scopes);
        }
        // [표준] nonce/id_token 검증을 위해 openid는 강제 포함
        final LinkedHashSet<String> withOpenId = new LinkedHashSet<>();
        withOpenId.add("openid");
        withOpenId.addAll(scopes);
        return withOpenId;
    }
}
