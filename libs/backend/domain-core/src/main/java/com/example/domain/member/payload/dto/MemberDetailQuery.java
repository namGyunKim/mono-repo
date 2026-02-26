package com.example.domain.member.payload.dto;

/**
 * 회원 상세 조회 요청 DTO
 * <p>
 * - Controller -> Service 경계에서 파라미터 나열을 방지하기 위한 전용 DTO입니다.
 */
public record MemberDetailQuery(Long id) {

    public static MemberDetailQuery of(Long id) {
        return new MemberDetailQuery(id);
    }
}
