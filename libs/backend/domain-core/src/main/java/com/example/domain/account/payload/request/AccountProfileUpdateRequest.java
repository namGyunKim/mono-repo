package com.example.domain.account.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "내 프로필 수정 요청")
public record AccountProfileUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Schema(description = "닉네임", example = "닉네임변경1")
        String nickName,

        @Schema(description = "비밀번호 (변경 시에만 입력)", example = "1234")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {

    public static AccountProfileUpdateRequest of(String nickName, String password) {
        return new AccountProfileUpdateRequest(nickName, password);
    }
}
