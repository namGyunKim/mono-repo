package com.example.domain.social.google.service.command;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthSession;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.support.GoogleOauthSessionResolver;
import com.example.global.security.payload.LoginTokenIssueCommand;
import com.example.global.security.service.command.LoginTokenCommandService;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GoogleSocialRedirectCommandService {

    private final GoogleOauthSessionResolver googleOauthSessionResolver;
    private final GoogleSocialCommandService googleSocialCommandService;
    private final LoginTokenCommandService loginTokenCommandService;

    public LoginTokenResponse loginByRedirect(GoogleSocialRedirectCommand command) {
        GoogleOauthSession oauthSession = googleOauthSessionResolver.resolveAndConsume(command);

        Long memberId = googleSocialCommandService.registerOrLoginBySocialCode(
                GoogleOauthLoginCommand.of(command.code(), oauthSession.codeVerifier(), oauthSession.nonce())
        );
        log.info("traceId={}, Google 소셜 로그인 성공: memberId={}", TraceIdUtils.resolveTraceId(), memberId);

        LoginTokenResponse response = loginTokenCommandService.issueTokens(LoginTokenIssueCommand.of(memberId));
        log.info(
                "traceId={}, Google 소셜 로그인 토큰 발급 완료: memberId={}, refreshTokenIssued={}",
                TraceIdUtils.resolveTraceId(),
                memberId,
                response.refreshToken() != null
        );
        return response;
    }
}
