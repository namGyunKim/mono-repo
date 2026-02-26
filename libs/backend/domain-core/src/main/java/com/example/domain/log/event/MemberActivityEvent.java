package com.example.domain.log.event;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityPayload;

/**
 * 회원 활동 로그 이벤트
 *
 * <p>
 * [네이밍 규칙 표준화]
 * - from(...): 외부 입력(로그인 ID, 타입, 상세 등)으로 이벤트 객체를 구성
 * </p>
 */
public record MemberActivityEvent(
        String loginId,     // 대상 회원 ID
        Long memberId,      // 대상 회원 고유 번호
        LogType logType,
        String details,
        String clientIp
) {

    public static MemberActivityEvent from(MemberActivityPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("payload는 필수입니다.");
        }
        return of(
                payload.loginId(),
                payload.memberId(),
                payload.logType(),
                payload.details(),
                payload.clientIp()
        );
    }

    public static MemberActivityEvent of(String loginId, Long memberId, LogType logType, String details, String clientIp) {
        return new MemberActivityEvent(loginId, memberId, logType, details, clientIp);
    }

}
