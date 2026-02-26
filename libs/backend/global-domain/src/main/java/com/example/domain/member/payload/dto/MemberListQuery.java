package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.domain.member.payload.request.MemberListRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 회원 목록 조회 Query DTO
 * <p>
 * - record 기반으로 불변성을 유지합니다.
 * - 컨트롤러/서비스 경계에서 입력(request) 객체를 이 Query로 변환합니다.
 */
public record MemberListQuery(
        @Schema(description = "페이지 번호", example = "1")
        @Positive(message = "페이지 번호는 1 이상이어야 합니다.")
        int page,
        @Schema(description = "페이지 사이즈", example = "10")
        @Positive(message = "페이지 사이즈는 1 이상이어야 합니다.")
        int size,
        @NotNull(message = "정렬 기준을 선택해주세요.")
        MemberOrderType order,
        @Schema(description = "검색어", example = "관리자")
        String searchWord,
        @NotNull(message = "필터 기준을 선택해주세요.")
        MemberFilterType filter,
        @NotNull(message = "활성화 여부를 선택해주세요.")
        MemberActiveStatus active
) {

    private MemberListQuery(MemberListRequest request) {
        this(request.page(), request.size(), request.order(), request.searchWord(), request.filter(), request.active());
    }

    public static MemberListQuery of(MemberListRequest request) {
        return from(request);
    }

    public static MemberListQuery from(MemberListRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return new MemberListQuery(request);
    }
}
