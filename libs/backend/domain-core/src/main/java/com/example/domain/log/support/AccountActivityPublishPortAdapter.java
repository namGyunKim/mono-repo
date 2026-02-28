package com.example.domain.log.support;

import com.example.domain.account.support.AccountActivityPublishPort;
import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.ActivityEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActivityPublishPortAdapter implements AccountActivityPublishPort {

    private final ActivityEventPublisher activityEventPublisher;

    @Override
    public void publishMemberActivity(String loginId, Long memberId, LogType logType, String details) {
        activityEventPublisher.publishMemberActivity(MemberActivityCommand.of(loginId, memberId, logType, details));
    }
}
