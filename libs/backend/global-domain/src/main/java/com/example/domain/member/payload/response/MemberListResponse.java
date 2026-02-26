package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 목록 응답
 * <p>
 * - GEMINI 규칙: 응답은 record를 사용합니다.
 */
public record MemberListResponse(
        MemberProfileListResponse profile
) {

    private MemberListResponse(Member member) {
        this(MemberProfileListResponse.from(member));
    }

    public static MemberListResponse of(Member member) {
        return from(member);
    }

    public static MemberListResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberListResponse(MemberProfileListResponse.from(member));
    }

}
