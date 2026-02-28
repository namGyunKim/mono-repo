package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.domain.member.payload.request.MemberListRequest;

/**
 * 회원 목록 조회 Query DTO
 * <p>
 * - record 기반으로 불변성을 유지합니다.
 * - 컨트롤러/서비스 경계에서 입력(request) 객체를 이 Query로 변환합니다.
 */
public record MemberListQuery(
        int page,
        int size,
        MemberOrderType order,
        String searchWord,
        MemberFilterType filter,
        MemberActiveStatus active
) {

    private MemberListQuery(MemberListRequest request) {
        this(
                request.page(),
                request.size(),
                request.toDomainOrder(),
                request.searchWord(),
                request.toDomainFilter(),
                request.toDomainActive()
        );
    }

    public static MemberListQuery from(MemberListRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return new MemberListQuery(request);
    }
}
