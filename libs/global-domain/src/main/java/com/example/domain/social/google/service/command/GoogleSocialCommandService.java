package com.example.domain.social.google.service.command;

import com.example.domain.social.google.payload.dto.GoogleOauthLoginCommand;
import com.example.domain.social.google.payload.dto.GoogleOauthResult;
import com.example.domain.social.google.payload.dto.GoogleSocialUnlinkCommand;
import com.example.domain.social.google.support.GoogleOauthProcessor;
import com.example.domain.social.google.support.GoogleSocialMemberRegistrar;
import com.example.domain.social.google.support.GoogleSocialUnlinkHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GoogleSocialCommandService {

    private final GoogleOauthProcessor googleOauthProcessor;
    private final GoogleSocialMemberRegistrar googleSocialMemberRegistrar;
    private final GoogleSocialUnlinkHandler googleSocialUnlinkHandler;

    public GoogleSocialCommandService(GoogleOauthProcessor googleOauthProcessor,
                                      GoogleSocialMemberRegistrar googleSocialMemberRegistrar,
                                      GoogleSocialUnlinkHandler googleSocialUnlinkHandler) {
        this.googleOauthProcessor = googleOauthProcessor;
        this.googleSocialMemberRegistrar = googleSocialMemberRegistrar;
        this.googleSocialUnlinkHandler = googleSocialUnlinkHandler;
    }

    /**
     * PKCE + nonce 검증을 포함한 구글 소셜 로그인 처리
     */
    public Long registerOrLoginBySocialCode(GoogleOauthLoginCommand command) {
        GoogleOauthResult result = googleOauthProcessor.authenticate(command);
        return googleSocialMemberRegistrar.registerOrLogin(result.userInfo(), result.refreshToken()).getId();
    }

    // 구글 연동 해제
    public void unlink(GoogleSocialUnlinkCommand command) {
        googleSocialUnlinkHandler.unlink(command);
    }
}
