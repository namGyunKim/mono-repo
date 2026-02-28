package com.example.domain.account.payload.dto;

import com.example.domain.log.enums.LogType;

/**
 * account -> log 도메인 활동 로그 발행 Command DTO
 */
public record AccountActivityPublishCommand(
        String loginId,
        Long memberId,
        LogType logType,
        String details
) {

    public static AccountActivityPublishCommand of(String loginId, Long memberId, LogType logType, String details) {
        return new AccountActivityPublishCommand(loginId, memberId, logType, details);
    }
}
