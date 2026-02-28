package com.example.domain.log.support;

import com.example.domain.account.payload.dto.AccountActivityPublishCommand;
import com.example.domain.account.support.AccountActivityPublishPort;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.LogActivityPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountActivityPublishPortAdapter implements AccountActivityPublishPort {

    private final LogActivityPublisher activityEventPublisher;

    @Override
    public void publishMemberActivity(AccountActivityPublishCommand command) {
        activityEventPublisher.publishMemberActivity(MemberActivityCommand.of(command.loginId(), command.memberId(), command.logType(), command.details()));
    }
}
