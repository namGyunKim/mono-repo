package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

/**
 * 회원 활동 로그 이벤트 발행용 커맨드 DTO
 */
public record MemberActivityCommand(
        String loginId,
        Long memberId,
        LogType logType,
        String details
) {

    public static MemberActivityCommand of(String loginId, Long memberId, LogType logType, String details) {
        return new MemberActivityCommand(loginId, memberId, logType, details);
    }

    public static MemberActivityCommand from(MemberActivityPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload는 필수입니다.");
        }
        return of(payload.loginId(), payload.memberId(), payload.logType(), payload.details());
    }
}
