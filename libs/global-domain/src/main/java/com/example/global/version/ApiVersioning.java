package com.example.global.version;

/**
 * API Versioning 공통 상수
 *
 * <p>
 * Spring Framework 7 (Spring Boot 4)의 "First-class API Versioning" 기능을 사용합니다.
 * - 기본 전략: 요청 헤더 기반 (API-Version)
 * - 기본 버전: 0.0 (요청에 버전이 없으면 0.0으로 처리, 유효하지 않음)
 * - 프론트엔드는 반드시 1.0을 명시적으로 전송해야 합니다.
 * </p>
 */
public final class ApiVersioning {

    /**
     * API 버전 요청 헤더 이름
     */
    public static final String HEADER_NAME = "API-Version";

    /**
     * v0 버전 (기본)
     */
    public static final String V0 = "0.0";

    /**
     * v1 버전
     */
    public static final String V1 = "1.0";

    /**
     * 요청에 버전 정보가 없을 때 적용되는 기본 버전
     */
    public static final String DEFAULT_VERSION = V0;

    /**
     * v1 베이스라인
     * - "1.0+"는 1.0 이상을 의미합니다.
     * - v2가 일부 엔드포인트에만 도입되더라도, v1 베이스라인 엔드포인트는 2.0 요청을 그대로 처리할 수 있습니다.
     */
    public static final String V1_BASELINE = "1.0+";

    /**
     * v2 예시 버전
     */
    public static final String V2 = "2.0";

    /**
     * v3 예시 버전
     */
    public static final String V3 = "3.0";

    /**
     * 지원하는 API 버전 목록
     */
    public static final java.util.List<String> SUPPORTED_VERSIONS = java.util.List.of(V1, V2, V3);

    private ApiVersioning() {
    }

    public static boolean isSupportedVersion(String version) {
        if (version == null || version.isBlank()) {
            return false;
        }
        return SUPPORTED_VERSIONS.contains(version.trim());
    }
}
