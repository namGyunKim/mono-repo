package com.example.domain.social.google.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleSocialActivityPublisher {

    private final ActivityEventPublisher activityEventPublisher;

    public void publishLogin(Member member) {
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(member.getLoginId(), member.getId(), LogType.LOGIN, "GOOGLE 로그인")
        );
    }

    public void publishJoin(Member member) {
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(member.getLoginId(), member.getId(), LogType.JOIN, "GOOGLE 회원가입")
        );
    }
}
