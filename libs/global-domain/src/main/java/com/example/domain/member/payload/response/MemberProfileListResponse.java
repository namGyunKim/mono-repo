package com.example.domain.member.payload.response;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;

/**
 * 회원 목록/요약 프로필 응답
 * <p>
 * - GEMINI 규칙: 응답은 record 사용
 */
public record MemberProfileListResponse(
        Long id,
        String loginId,
        String nickName,
        AccountRole role,
        MemberType memberType
) {

    private MemberProfileListResponse(Member member) {
        this(member.getId(), member.getLoginId(), member.getNickName(), member.getRole(), member.getMemberType());
    }

    public static MemberProfileListResponse of(Member member) {
        return from(member);
    }

    public static MemberProfileListResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberProfileListResponse(member);
    }
}
