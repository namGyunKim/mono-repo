package com.example.domain.member.payload.dto;

import com.example.domain.member.enums.MemberUploadDirect;

/**
 * 회원 이미지 조회용 DTO
 *
 * <p>
 * - GEMINI 규칙: DTO는 record 사용
 * - 외부에서는 new 생성자 호출 대신 정적 팩토리 메서드(of)를 사용합니다.
 * </p>
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
