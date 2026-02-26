package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

import java.time.LocalDateTime;

/**
 * 회원 활동 로그 조회 Projection DTO
 */
public record MemberLogView(
        Long id,
        String loginId,
        String executorId,
        LogType logType,
        String details,
        String clientIp,
        LocalDateTime createdAt
) {

    public static MemberLogView of(
            Long id,
            String loginId,
            String executorId,
            LogType logType,
            String details,
            String clientIp,
            LocalDateTime createdAt
    ) {
        return new MemberLogView(id, loginId, executorId, logType, details, clientIp, createdAt);
    }
}
