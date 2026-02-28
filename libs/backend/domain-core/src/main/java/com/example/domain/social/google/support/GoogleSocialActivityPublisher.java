package com.example.domain.social.google.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.social.support.SocialActivityPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleSocialActivityPublisher {

    private final SocialActivityPublishPort socialActivityPublishPort;

    public void publishLogin(String loginId, Long memberId) {
        socialActivityPublishPort.publishMemberActivity(loginId, memberId, LogType.LOGIN, "GOOGLE 로그인");
    }

    public void publishJoin(String loginId, Long memberId) {
        socialActivityPublishPort.publishMemberActivity(loginId, memberId, LogType.JOIN, "GOOGLE 회원가입");
    }
}
