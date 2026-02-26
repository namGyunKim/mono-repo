package com.example.global.exception.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum ErrorCode {

    //  === COMMON (0000) ===
    REQUEST_BINDING_RESULT("0001", "Request 바인딩 에러"),
    INPUT_VALUE_INVALID("0002", "유효하지 않은 입력 값입니다."),
    INVALID_PARAMETER("0003", "잘못된 파라미터입니다."),
    INTERNAL_SERVER_ERROR("0004", "내부 서버 오류"),
    METHOD_NOT_SUPPORTED("0005", "지원하지 않는 메소드입니다."),
    PAGE_NOT_EXIST("0006", "페이지를 찾을 수 없습니다."), // 추가됨: 404 처리용
    RATE_LIMIT_EXCEEDED("0007", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // Spring Boot 4 / Spring Framework 7: API 버저닝
    API_VERSION_REQUIRED("0008", "API 버전이 필요합니다."),
    API_VERSION_INVALID("0009", "유효하지 않은 API 버전입니다."),

    // === AUTH (1000) ===
    AUTHENTICATION_REQUIRED("1001", "인증이 필요합니다."),
    AUTHENTICATION_FAILED("1002", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCESS_DENIED("1004", "권한이 없습니다."),
    SOCIAL_TOKEN_ERROR("1006", "소셜 토큰 에러"),
    REFRESH_TOKEN_INVALID("1007", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED("1008", "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_REVOKED("1009", "이미 폐기된 리프레시 토큰입니다."),

    // === MEMBER (1100) ===
    MEMBER_NOT_EXIST("1101", "존재하지 않는 회원입니다."),
    MEMBER_INACTIVE("1102", "비활성화된 계정입니다."),
    MEMBER_ALREADY_EXIST("1103", "이미 존재하는 회원입니다."),

    // === SOCIAL (1200) ===
    GOOGLE_API_GET_CODE_ERROR("1206", "구글 API 코드 획득 실패"),
    GOOGLE_API_GET_TOKEN_ERROR("1207", "구글 API 토큰 획득 실패"),
    GOOGLE_API_GET_INFORMATION_ERROR("1208", "구글 API 사용자 정보 조회 실패"),
    GOOGLE_API_UNLINK_ERROR("1209", "구글 API 연동 해제 실패"),

    // [보안] OAuth/OIDC 확장 대비
    GOOGLE_OAUTH_STATE_MISMATCH("1210", "구글 OAuth state 검증 실패"),
    GOOGLE_OAUTH_ID_TOKEN_MISSING("1211", "구글 OAuth id_token 누락"),
    GOOGLE_OAUTH_NONCE_MISMATCH("1212", "구글 OAuth nonce 불일치"),
    GOOGLE_OAUTH_INVALID_ID_TOKEN("1213", "구글 OAuth id_token 처리 실패"),

    // === FILE/IMAGE (5000) ===
    INVALID_IMAGE_FILE("5001", "유효하지 않은 이미지 파일입니다."),
    INVALID_IMAGE_DIMENSIONS("5002", "이미지 규격이 올바르지 않습니다."), // 추가됨: 이미지 크기 검증용
    FILE_SIZE_EXCEEDED("5003", "파일 크기가 제한을 초과했습니다."),
    FILE_IS_EMPTY("5004", "파일이 비어있습니다."),
    UNSUPPORTED_FILE_EXTENSION("5005", "지원하지 않는 파일 확장자입니다."),
    FILE_UPLOAD_FAILED("5007", "파일 업로드에 실패했습니다."),
    FILE_DOWNLOAD_FAILED("5008", "파일 다운로드에 실패했습니다."),
    FILE_NOT_FOUND("9011", "파일을 찾을 수 없습니다."),

    // === OTHER (9000) ===
    DATA_ACCESS_ERROR("9003", "데이터베이스 접근 에러"),
    FAILED("9999", "예상치 못한 오류");

    private static final Map<String, ErrorCode> errorMap =
            Arrays.stream(values()).collect(Collectors.toMap(ErrorCode::getCode, e -> e));

    private final String code;
    private final String errorMessage;

    ErrorCode(String code, String msg) {
        this.code = code;
        this.errorMessage = msg;
    }

    public static ErrorCode getByCode(String code) {
        return errorMap.get(code);
    }

    public Map<String, String> getErrorMap() {
        return Map.of(
                "code", code,
                "message", errorMessage
        );
    }
}
