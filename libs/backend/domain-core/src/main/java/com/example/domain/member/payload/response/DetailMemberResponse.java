package com.example.domain.member.payload.response;

import com.example.domain.member.entity.Member;

/**
 * 회원 상세 응답
 */
public record DetailMemberResponse(
        MemberProfileDetailResponse profile
) {

    public static DetailMemberResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new DetailMemberResponse(MemberProfileDetailResponse.from(member));
    }

}
