package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 목록 응답
 */
public record MemberListResponse(
        MemberProfileListResponse profile
) {

    public static MemberListResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberListResponse(MemberProfileListResponse.from(member));
    }

}
