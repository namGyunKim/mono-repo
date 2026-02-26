package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

/**
 * 회원 활동 이벤트 생성용 Payload DTO
 */
public record MemberActivityPayload(
        String loginId,
        Long memberId,
        LogType logType,
        String details,
        String clientIp
) {

    public static MemberActivityPayload of(
            String loginId,
            Long memberId,
            LogType logType,
            String details,
            String clientIp
    ) {
        return new MemberActivityPayload(loginId, memberId, logType, details, clientIp);
    }
}
