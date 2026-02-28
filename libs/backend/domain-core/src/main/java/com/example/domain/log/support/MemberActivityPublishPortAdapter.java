package com.example.domain.log.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.LogActivityPublisher;
import com.example.domain.member.support.MemberActivityPublishPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberActivityPublishPortAdapter implements MemberActivityPublishPort {

    private final LogActivityPublisher activityEventPublisher;

    @Override
    public void publishMemberActivity(String loginId, Long memberId, LogType logType, String details) {
        activityEventPublisher.publishMemberActivity(MemberActivityCommand.of(loginId, memberId, logType, details));
    }
}
