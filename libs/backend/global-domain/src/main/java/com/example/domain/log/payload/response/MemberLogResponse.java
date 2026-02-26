package com.example.domain.log.payload.response;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberLogView;
import com.example.global.utils.DateTimeFormatUtils;

public record MemberLogResponse(
        Long id,
        String loginId,     // 대상 회원 ID
        String executorId,  // 수행자 ID (기존 executorId 대신 createdBy 매핑)
        LogType logType,    // 로그 유형
        String details,     // 상세 내용
        String clientIp,    // IP 주소
        String createdAt    // 생성일시
) {
    private MemberLogResponse(MemberLogView view) {
        this(
                view.id(),
                view.loginId(),
                view.executorId(),
                view.logType(),
                view.details(),
                view.clientIp(),
                DateTimeFormatUtils.formatKoreanDateTime(view.createdAt())
        );
    }

    public static MemberLogResponse from(MemberLogView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }
        return new MemberLogResponse(view);
    }
}
