package com.example.domain.member.payload.response;

import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.entity.Member;

/**
 * 회원 목록/요약 프로필 응답
 */
public record MemberProfileListResponse(
        Long id,
        String loginId,
        String nickName,
        ApiAccountRole role,
        ApiMemberType memberType
) {

    private MemberProfileListResponse(Member member) {
        this(
                member.getId(),
                member.getLoginId(),
                member.getNickName(),
                ApiAccountRole.fromDomain(member.getRole()),
                ApiMemberType.fromDomain(member.getMemberType())
        );
    }

    public static MemberProfileListResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberProfileListResponse(member);
    }
}
