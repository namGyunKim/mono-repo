package com.example.global.security.handler.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.LogActivityPublisher;
import com.example.domain.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginSuccessEventPublisher {

    private static final String DEFAULT_MESSAGE = "로그인 성공";

    private final LogActivityPublisher activityEventPublisher;

    public void publish(PrincipalDetails principal, String message) {
        if (principal == null) {
            return;
        }

        final String resolvedMessage = (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message;
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        principal.getUsername(),
                        principal.getId(),
                        LogType.LOGIN,
                        resolvedMessage
                )
        );
    }
}
