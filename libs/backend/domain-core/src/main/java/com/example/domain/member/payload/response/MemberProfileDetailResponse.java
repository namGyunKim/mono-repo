package com.example.domain.member.payload.response;

import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.entity.Member;
import com.example.global.utils.DateTimeFormatUtils;

/**
 * 회원 상세 프로필 응답
 * <p>
 * - GEMINI 규칙: 응답은 record 사용
 * - 감사(Audit) 정보를 포함합니다.
 */
public record MemberProfileDetailResponse(
        Long id,
        String loginId,
        String nickName,
        ApiAccountRole role,
        ApiMemberType memberType,
        String createdAt,
        String createdBy,
        String modifiedAt,
        String modifiedBy
) {

    private MemberProfileDetailResponse(Member member) {
        this(
                member.getId(),
                member.getLoginId(),
                member.getNickName(),
                ApiAccountRole.fromDomain(member.getRole()),
                ApiMemberType.fromDomain(member.getMemberType()),
                DateTimeFormatUtils.formatKoreanDateTime(member.getCreatedAt()),
                member.getCreatedBy(),
                DateTimeFormatUtils.formatKoreanDateTime(member.getModifiedAt()),
                member.getLastModifiedBy()
        );
    }

    public static MemberProfileDetailResponse of(Member member) {
        return from(member);
    }

    public static MemberProfileDetailResponse from(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        return new MemberProfileDetailResponse(member);
    }
}
