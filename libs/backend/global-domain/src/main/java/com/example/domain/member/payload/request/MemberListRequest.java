package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.global.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MemberListRequest(
        @NotNull(message = "권한은 필수입니다.")
        @Schema(description = "권한", example = "USER")
        AccountRole role,

        @Schema(description = "페이지 번호", example = "1")
        Integer page,

        @Schema(description = "페이지 사이즈", example = "10")
        Integer size,

        @Schema(description = "정렬 기준")
        MemberOrderType order,

        @Schema(description = "검색어", example = "검색어")
        String searchWord,

        @Schema(description = "필터 기준")
        MemberFilterType filter,

        @Schema(description = "활성화 여부")
        MemberActiveStatus active
) {

    // 생성자에서 null 또는 유효하지 않은 값에 대한 기본값 설정
    public MemberListRequest {
        page = PaginationUtils.normalizePage(page);
        size = PaginationUtils.normalizeSize(size, PaginationUtils.DEFAULT_SIZE);
        if (order == null) order = MemberOrderType.CREATE_DESC; // 기본 정렬: 최신순
        if (searchWord == null) searchWord = "";
        if (filter == null) filter = MemberFilterType.ALL;
        if (active == null) active = MemberActiveStatus.ALL;
    }

    public static MemberListRequest of(
            AccountRole role,
            Integer page,
            Integer size,
            MemberOrderType order,
            String searchWord,
            MemberFilterType filter,
            MemberActiveStatus active
    ) {
        return new MemberListRequest(role, page, size, order, searchWord, filter, active);
    }

    public static MemberListRequest defaultRequest() {
        return of(AccountRole.USER, PaginationUtils.DEFAULT_PAGE, PaginationUtils.DEFAULT_SIZE, null, "", null, null);
    }
}
