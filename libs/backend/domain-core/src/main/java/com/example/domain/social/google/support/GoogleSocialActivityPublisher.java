package com.example.domain.social.google.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.member.entity.Member;
import com.example.domain.social.support.SocialActivityPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleSocialActivityPublisher {

    private final SocialActivityPublishPort socialActivityPublishPort;

    public void publishLogin(Member member) {
        socialActivityPublishPort.publishMemberActivity(member.getLoginId(), member.getId(), LogType.LOGIN, "GOOGLE 로그인");
    }

    public void publishJoin(Member member) {
        socialActivityPublishPort.publishMemberActivity(member.getLoginId(), member.getId(), LogType.JOIN, "GOOGLE 회원가입");
    }
}
