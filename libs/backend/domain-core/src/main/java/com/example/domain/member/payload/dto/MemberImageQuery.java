package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberUploadDirect;

/**
 * 회원 이미지 조회용 DTO
 */
public record MemberImageQuery(
        String fileName,
        MemberUploadDirect uploadDirect
) {

    public static MemberImageQuery of(String fileName, MemberUploadDirect uploadDirect) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName은 필수입니다.");
        }
        if (uploadDirect == null) {
            throw new IllegalArgumentException("uploadDirect는 필수입니다.");
        }
        return new MemberImageQuery(fileName, uploadDirect);
    }
}
