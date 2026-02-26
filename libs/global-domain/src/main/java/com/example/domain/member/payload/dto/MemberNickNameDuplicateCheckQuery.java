package com.example.domain.member.payload.dto;

/**
 * 닉네임 중복 체크용 조회 DTO
 */
public record MemberNickNameDuplicateCheckQuery(String nickName) {

    public static MemberNickNameDuplicateCheckQuery of(String nickName) {
        return new MemberNickNameDuplicateCheckQuery(nickName);
    }
}
