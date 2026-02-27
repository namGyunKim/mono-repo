package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberActiveStatus;
import com.example.domain.contract.enums.ApiMemberFilterType;
import com.example.domain.contract.enums.ApiMemberOrderType;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.global.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberListRequest(
        @NotNull(message = "권한은 필수입니다.")
        @Schema(description = "권한", example = "USER")
        ApiAccountRole role,

        @Schema(description = "페이지 번호", example = "1")
        Integer page,

        @Schema(description = "페이지 사이즈", example = "10")
        Integer size,

        @Schema(description = "정렬 기준")
        ApiMemberOrderType order,

        @Schema(description = "검색어", example = "검색어")
        String searchWord,

        @Schema(description = "필터 기준")
        ApiMemberFilterType filter,

        @Schema(description = "활성화 여부")
        ApiMemberActiveStatus active
) {

    // 생성자에서 null 또는 유효하지 않은 값에 대한 기본값 설정
    public MemberListRequest {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_SIZE);
        if (order == null) order = ApiMemberOrderType.CREATE_DESC; // 기본 정렬: 최신순
        if (searchWord == null) searchWord = "";
        if (filter == null) filter = ApiMemberFilterType.ALL;
        if (active == null) active = ApiMemberActiveStatus.ALL;
    }

    public static MemberListRequest of(
            ApiAccountRole role,
            Integer page,
            Integer size,
            ApiMemberOrderType order,
            String searchWord,
            ApiMemberFilterType filter,
            ApiMemberActiveStatus active
    ) {
        return new MemberListRequest(role, page, size, order, searchWord, filter, active);
    }

    public static MemberListRequest defaultRequest() {
        return of(ApiAccountRole.USER, PaginationUtils.DEFAULT_PAGE, PaginationUtils.DEFAULT_SIZE, null, "", null, null);
    }

    public AccountRole toDomainRole() {
        return role != null ? role.toDomain() : null;
    }

    public MemberOrderType toDomainOrder() {
        return order != null ? order.toDomain() : null;
    }

    public MemberFilterType toDomainFilter() {
        return filter != null ? filter.toDomain() : null;
    }

    public MemberActiveStatus toDomainActive() {
        return active != null ? active.toDomain() : null;
    }
}
