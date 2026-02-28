package com.example.domain.aws.enums;

import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ImageType {
    // === Member Related (New) ===
    MEMBER_PROFILE("회원 프로필 이미지", "member/profile/");

    // --- Static Map ---
    private static final Map<String, ImageType> NAME_MAP = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(
                    type -> type.name().toUpperCase(), // Key: Enum 이름 (대문자)
                    Function.identity()               // Value: Enum 상수 자신
            ));

    // Member
    private static final List<ImageType> MEMBER_UPLOAD_ALLOWED_TYPES = List.of(
            MEMBER_PROFILE
    );

    // --- Fields ---
    private final String description;
    private final String path;
    private final Integer width;
    private final Integer height;
    private final List<String> allowedExtensions;
    private final long maxSize;

    // --- Constructors ---
    ImageType(String description, String path) {
        this(description, path, null, null, List.of("jpg", "jpeg", "png", "webp"), 10 * 1024 * 1024L);
    }

    // --- Static Helper Methods ---

    /**
     * 요청 값(String)으로 Enum을 매칭합니다.
     * <p>
     * [네이밍 표준]
     * - from(...): 외부 입력값을 Enum으로 변환하는 표준 메서드
     * </p>
     */
    @JsonCreator
    public static ImageType from(String requestValue) {
        if (requestValue == null) {
            return null;
        }
        return NAME_MAP.get(requestValue.toUpperCase());
    }

    public static ImageType getByName(String name) {
        return from(name);
    }

    // --- Allowed Type Lists ---

    public static List<ImageType> getAllTypes() {
        return List.of(values());
    }

    // --- Static Mapping/Validation Methods ---

    // Member (S3MemberCommandService에서 필요)
    public static void validateMemberUploadType(ImageType type) {
        if (type == null || !MEMBER_UPLOAD_ALLOWED_TYPES.contains(type)) {
            throw new GlobalException(ErrorCode.INPUT_VALUE_INVALID, "회원 업로드에 허용되지 않는 이미지 타입입니다: " + type);
        }
    }

    public void validateExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new GlobalException(ErrorCode.UNSUPPORTED_FILE_EXTENSION, "파일 확장자가 없습니다.");
        }

        boolean match = this.allowedExtensions.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));

        if (!match) {
            String allowed = String.join(", ", this.allowedExtensions);
            throw new GlobalException(ErrorCode.UNSUPPORTED_FILE_EXTENSION, "허용되지 않는 파일 확장자입니다. (" + extension + "). 허용된 확장자: " + allowed);
        }
    }
}
