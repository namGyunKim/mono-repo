package com.example.global.payload.response;

/**
 * 루트("/") 엔드포인트 응답 DTO
 *
 * <p>
 * - 브라우저가 서버 주소(예: http://localhost:8080/)로 접근했을 때,
 * 불필요한 정적 리소스 404 경고 로그가 발생하지 않도록 간단한 정보를 제공합니다.
 * - DTO 규칙: record + 정적 팩토리 메서드(of)
 * </p>
 */
public record RootInfoResponse(
        String message,
        String swaggerUiUrl,
        String healthCheckUrl
) {

    public RootInfoResponse {
        if (message == null) {
            message = "";
        }
        if (swaggerUiUrl == null) {
            swaggerUiUrl = "";
        }
        if (healthCheckUrl == null) {
            healthCheckUrl = "";
        }
    }

    public static RootInfoResponse of(boolean swaggerUiEnabled) {
        String swaggerUiUrl = swaggerUiEnabled ? "/swagger-ui.html" : "";

        return of(
                "REST API 서버가 실행 중입니다.",
                swaggerUiUrl,
                "/api/health"
        );
    }

    public static RootInfoResponse of(String message, String swaggerUiUrl, String healthCheckUrl) {
        return new RootInfoResponse(message, swaggerUiUrl, healthCheckUrl);
    }
}
