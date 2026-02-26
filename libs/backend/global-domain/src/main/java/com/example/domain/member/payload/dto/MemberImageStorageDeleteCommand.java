package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberUploadDirect;

public record MemberImageStorageDeleteCommand(
        Long memberId,
        String fileName,
        MemberUploadDirect uploadDirect
) {

    public static MemberImageStorageDeleteCommand of(Long memberId, String fileName, MemberUploadDirect uploadDirect) {
        return new MemberImageStorageDeleteCommand(memberId, fileName, uploadDirect);
    }
}
