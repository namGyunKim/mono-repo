package com.example.domain.security.adapter;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.security.token.LoginTokenCommandService;
import com.example.domain.social.support.SocialLoginTokenPort;
import com.example.global.security.payload.LoginTokenIssueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialLoginTokenPortAdapter implements SocialLoginTokenPort {

    private final LoginTokenCommandService loginTokenCommandService;

    @Override
    public LoginTokenResponse issueTokens(Long memberId) {
        return loginTokenCommandService.issueTokens(LoginTokenIssueCommand.of(memberId));
    }
}
