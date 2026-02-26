package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.request.MemberLogRequest;
import com.example.global.utils.PaginationUtils;

import java.time.LocalDateTime;

/**
 * 회원 활동 로그 조회용 Query DTO
 *
 * <p>
 * - Controller ↔ Service 경계에서 요청 DTO를 직접 전달하지 않기 위해 분리합니다.
 * - page/size 보정은 동일 정책(PaginationUtils)을 적용합니다.
 * </p>
 */
public record MemberLogQuery(
        Integer page,
        Integer size,
        String loginId,
        Long memberId,
        LogType logType,
        String details,
        LocalDateTime startAt,
        LocalDateTime endAt
) {

    public MemberLogQuery {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_LOG_SIZE);
        loginId = normalizeKeyword(loginId);
        details = normalizeKeyword(details);
        if (startAt == null) {
            startAt = LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        if (endAt == null) {
            endAt = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
    }

    public static MemberLogQuery from(MemberLogRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return new MemberLogQuery(
                request.page(),
                request.size(),
                request.loginId(),
                request.memberId(),
                request.logType(),
                request.details(),
                request.startAt(),
                request.endAt()
        );
    }

    private static String normalizeKeyword(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
