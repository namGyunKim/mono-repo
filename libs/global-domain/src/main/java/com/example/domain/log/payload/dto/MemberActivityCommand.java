package com.example.domain.log.payload.dto;

import com.example.domain.log.enums.LogType;

/**
 * 회원 활동 로그 이벤트 발행용 커맨드 DTO
 *
 * <p>
 * - GEMINI 규칙: DTO는 record + 정적 팩토리 메서드(from/of)를 제공합니다.
 * - 서비스 간 파라미터 전달을 DTO로 일원화하여 변경 영향 범위를 최소화합니다.
 * </p>
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
