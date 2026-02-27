package com.example.domain.log.payload.request;

import com.example.domain.contract.enums.ApiLogType;
import com.example.domain.log.enums.LogType;
import com.example.global.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 회원 활동 로그 조회 요청 DTO
 *
 * <p>
 * 베이스 프로젝트에서는 화면/쿼리스트링 조작 등으로 page/size가 비정상 값으로 들어와도
 * 서비스에서 예외가 발생하지 않도록 기본값/보정을 수행합니다.
 * </p>
 */
public record MemberLogRequest(
        @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
        @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
        Integer page,

        @Schema(description = "페이지 사이즈", example = "20")
        @Min(value = 1, message = "페이지 사이즈는 1 이상이어야 합니다.")
        Integer size,

        @Schema(description = "로그인 아이디 (부분 검색)", example = "user")
        String loginId,

        @Schema(description = "회원 ID", example = "1001")
        Long memberId,

        @Schema(description = "로그 유형", example = "LOGIN")
        ApiLogType logType,

        @Schema(description = "상세 내용 (부분 검색)", example = "비밀번호")
        String details,

        @Schema(description = "조회 시작 일시 (ISO-8601)", example = "2026-02-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt,

        @Schema(description = "조회 종료 일시 (ISO-8601, 미만)", example = "2026-03-01T00:00:00")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endAt
) {

    public MemberLogRequest {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_LOG_SIZE);
        loginId = normalizeKeyword(loginId);
        details = normalizeKeyword(details);
    }

    public static MemberLogRequest of(
            Integer page,
            Integer size,
            String loginId,
            Long memberId,
            ApiLogType logType,
            String details,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return new MemberLogRequest(page, size, loginId, memberId, logType, details, startAt, endAt);
    }

    public LogType toDomainLogType() {
        return logType != null ? logType.toDomain() : null;
    }

    private static String normalizeKeyword(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
