package com.example.domain.account.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 로그인 요청")
public record AccountAdminLoginRequest(
        @NotBlank(message = "로그인 아이디를 입력해주세요.")
        @Schema(description = "로그인 아이디", example = "superAdmin")
        String loginId,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Schema(description = "비밀번호", example = "1234")
        @Size(min = 4, max = 20, message = "비밀번호는 4자 이상 20자 이하로 입력해주세요.")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    public static AccountAdminLoginRequest of(String loginId, String password) {
        return new AccountAdminLoginRequest(loginId, password);
    }
}
