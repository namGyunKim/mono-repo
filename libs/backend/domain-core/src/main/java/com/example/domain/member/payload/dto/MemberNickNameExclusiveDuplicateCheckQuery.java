package com.example.domain.member.payload.dto;

/**
 * 닉네임 중복 여부를 확인하기 위한 전용 DTO
 * <p>
 * - Controller -> Service 경계에서 파라미터 나열을 방지하기 위한 전용 DTO입니다.
 */
public record MemberNickNameExclusiveDuplicateCheckQuery(
        String nickName,
        String excludedLoginId
) {

    public static MemberNickNameExclusiveDuplicateCheckQuery of(String nickName, String excludedLoginId) {
        return new MemberNickNameExclusiveDuplicateCheckQuery(nickName, excludedLoginId);
    }
}
