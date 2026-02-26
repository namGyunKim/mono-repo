package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberUploadDirect;

public record MemberImageRegisterCommand(
        Long memberId,
        MemberUploadDirect uploadDirect,
        String fileName
) {

    public static MemberImageRegisterCommand of(Long memberId, MemberUploadDirect uploadDirect, String fileName) {
        return new MemberImageRegisterCommand(memberId, uploadDirect, fileName);
    }
}
