package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.event.MemberActivityEvent;

/**
 * 회원 활동 로그 엔티티 생성용 Command DTO
 */
public record MemberLogCreateCommand(
        String loginId,
        Long memberId,
        LogType logType,
        String details,
        String clientIp
) {

    public static MemberLogCreateCommand of(
            String loginId,
            Long memberId,
            LogType logType,
            String details,
            String clientIp
    ) {
        return new MemberLogCreateCommand(loginId, memberId, logType, details, clientIp);
    }

    public static MemberLogCreateCommand from(MemberActivityEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event는 필수입니다.");
        }
        return of(
                event.loginId(),
                event.memberId(),
                event.logType(),
                event.details(),
                event.clientIp()
        );
    }
}
