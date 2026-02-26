package com.example.domain.social.google.client;

import com.example.domain.social.google.payload.response.GoogleTokenResponse;
import com.example.domain.social.google.payload.response.GoogleUserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface GoogleApiClient {

    /**
     * 구글 OAuth 2.0 Access Token 및 ID Token을 가져오는 API 호출
     * <p>
     * - [보안] PKCE(code_verifier) 파라미터를 지원합니다.
     */
    @PostExchange(url = "/oauth2/v4/token", contentType = "application/x-www-form-urlencoded")
    ResponseEntity<GoogleTokenResponse> getToken(@RequestBody MultiValueMap<String, String> requestForm);

    /**
     * ID Token에서 사용자 정보를 추출하거나, 별도의 API를 통해 사용자 정보를 가져옵니다.
     * 구글의 /oauth2/v2/userinfo 엔드포인트는 GET 요청으로 access_token을 쿼리 파라미터로 전달하는 것이 일반적입니다.
     */
    @GetExchange("/oauth2/v2/userinfo")
    ResponseEntity<GoogleUserInfoResponse> getUserInfo(
            @RequestParam("access_token") String accessToken
    );
}
