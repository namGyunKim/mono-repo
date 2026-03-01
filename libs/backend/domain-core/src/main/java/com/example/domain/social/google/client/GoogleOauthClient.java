package com.example.domain.social.google.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface GoogleOauthClient {

    /**
     * Google Refresh 또는 Access Token을 취소(Revocation)하는 API 호출
     * <p>
     * - 응답 본문이 없거나 200 OK만 반환되므로 Void로 처리합니다.
     */
    @PostExchange(url = "/revoke", contentType = "application/x-www-form-urlencoded")
    ResponseEntity<Void> revokeToken(
            @RequestParam("token") final String token
    );
}
