package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.enums.MemberType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberCreateRequest(
        @NotBlank(message = "로그인 아이디를 입력해주세요.")
        @Schema(description = "로그인 아이디", example = "user1")
        String loginId,

        @NotBlank(message = "닉네임을 입력해주세요.")
        @Schema(description = "닉네임", example = "테스트1")
        String nickName,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Schema(description = "비밀번호", example = "1234")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,

        @Schema(description = "권한 (관리자 생성 시 필요, 사용자 생성 시 무시됨)", example = "USER")
        ApiAccountRole role,

        @NotNull(message = "멤버 타입을 선택해주세요.")
        @Schema(description = "멤버 타입", example = "GENERAL")
        ApiMemberType memberType
) {

    public static MemberCreateRequest of(String loginId, String nickName, String password, ApiAccountRole role, ApiMemberType memberType) {
        return new MemberCreateRequest(loginId, nickName, password, role, memberType);
    }

    /**
     * role 파라미터 변조 방어 용도
     * - 외부에서 new 호출을 금지하기 위해, 역할 고정은 DTO 내부에서 수행한다.
     */
    public MemberCreateRequest withRole(AccountRole role) {
        return of(loginId(), nickName(), password(), ApiAccountRole.fromDomain(role), memberType());
    }

    public AccountRole toDomainRole() {
        return role != null ? role.toDomain() : null;
    }

    public MemberType toDomainMemberType() {
        return memberType != null ? memberType.toDomain() : null;
    }
}
