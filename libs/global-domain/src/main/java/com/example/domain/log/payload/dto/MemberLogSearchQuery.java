package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

import java.time.LocalDateTime;

/**
 * 회원 활동 로그 검색용 조회 DTO
 */
public record MemberLogSearchQuery(
        String loginId,
        Long memberId,
        LogType logType,
        String details,
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public MemberLogSearchQuery {
        loginId = normalizeKeyword(loginId);
        details = normalizeKeyword(details);
        if (startAt == null) {
            startAt = LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        if (endAt == null) {
            endAt = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
    }

    public static MemberLogSearchQuery of(
            String loginId,
            Long memberId,
            LogType logType,
            String details,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return new MemberLogSearchQuery(loginId, memberId, logType, details, startAt, endAt);
    }

    public static MemberLogSearchQuery from(MemberLogQuery query) {
        if (query == null) {
            return of("", null, null, "", null, null);
        }
        return of(query.loginId(), query.memberId(), query.logType(), query.details(), query.startAt(), query.endAt());
    }

    private static String normalizeKeyword(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
