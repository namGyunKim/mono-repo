package com.example.domain.member.payload.dto;

public record MemberImageDeleteCommand(
        Long memberImageId
) {

    public static MemberImageDeleteCommand of(Long memberImageId) {
        return new MemberImageDeleteCommand(memberImageId);
    }
}
