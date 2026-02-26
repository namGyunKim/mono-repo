package com.example.domain.member.payload.response;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;
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
        AccountRole role,
        MemberType memberType,
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
                member.getRole(),
                member.getMemberType(),
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
