package com.example.global.security.handler.support;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.LogActivityPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class LoginFailureEventPublisher {

    private static final String DEFAULT_LOGIN_ID = "UNKNOWN";

    private final LogActivityPublisher activityEventPublisher;

    public void publishLoginFailEvent(String loginId, Long memberId, String detailMessage) {
        final String resolvedLoginId = StringUtils.hasText(loginId) ? loginId : DEFAULT_LOGIN_ID;
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        resolvedLoginId,
                        memberId,
                        LogType.LOGIN_FAIL,
                        detailMessage != null ? detailMessage : "로그인 실패"
                )
        );
    }
}
