package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 상세 응답
 * <p>
 * - GEMINI 규칙: 응답은 record를 사용합니다.
 */
public record DetailMemberResponse(
        MemberProfileDetailResponse profile
) {

    private DetailMemberResponse(Member member) {
        this(MemberProfileDetailResponse.from(member));
    }

    public static DetailMemberResponse of(Member member) {
        return from(member);
    }

    public static DetailMemberResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new DetailMemberResponse(MemberProfileDetailResponse.from(member));
    }

}
