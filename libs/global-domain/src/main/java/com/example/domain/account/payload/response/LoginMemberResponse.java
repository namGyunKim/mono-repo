package com.example.domain.account.payload.response;

import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.global.enums.GlobalActiveEnums;

/**
 * 로그인/프로필 화면에서 사용하는 회원 요약 DTO
 * <p>
 * - GEMINI 규칙: DTO는 record 사용
 */
public record LoginMemberResponse(
        Long id,
        String loginId,
        String role,
        String nickName,
        String memberType,
        GlobalActiveEnums active
) {

    public static LoginMemberResponse from(LoginMemberView view) {
        if (view == null) {
            throw new IllegalArgumentException("view는 필수입니다.");
        }

        return new LoginMemberResponse(
                view.id(),
                view.loginId(),
                view.role().getValue(),
                view.nickName(),
                view.memberType().name(),
                view.active()
        );
    }

}
