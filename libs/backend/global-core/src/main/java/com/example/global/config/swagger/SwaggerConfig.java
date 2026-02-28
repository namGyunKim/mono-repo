package com.example.global.config.swagger;

import com.example.global.security.SecurityHeaders;
import com.example.global.security.SecurityPublicPaths;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger 설정

@OpenAPIDefinition(info = @Info(title = "SAMPLE API"))
@Configuration
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER,
        paramName = SecurityHeaders.AUTHORIZATION)
public class SwaggerConfig {

    private static final String HEALTH_CHECK_PATH = "/api/health";
    private static final String SOCIAL_API_BASE_PATH = "/api/social";
    private static final String BEARER_SECURITY_SCHEME = "Bearer Authentication";

    @Bean
    public GroupedOpenApi openApi() {
        return GroupedOpenApi.builder()
                .group("api")
                // REST API 문서 기준: /api/**
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * Swagger UI에서 API Version 헤더를 쉽게 테스트할 수 있도록, 전역 헤더 파라미터를 추가합니다.
     *
     * <p>
     * - API 버저닝은 URL 경로(/v1 등)가 아니라, 요청 헤더(API-Version)로만 처리합니다.
     * - 예외:
     * - 헬스체크(/api/health)
     * - 소셜 로그인(/api/social/**) : 외부 OAuth Provider가 API-Version 헤더를 전달할 수 없음
     * </p>
     */
    @Bean
    public OpenApiCustomizer apiVersionHeaderCustomizer() {
        return openApi -> {
            if (openApi == null || openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach(this::addApiVersionHeaderIfRequired);
        };
    }

    /**
     * Swagger UI에서 Authorize 토큰을 실제 요청에 반영하기 위해,
     * API 경로에 Bearer 인증 요구사항을 기본으로 등록합니다.
     */
    @Bean
    public OpenApiCustomizer bearerAuthCustomizer() {
        return openApi -> {
            if (openApi == null || openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach(this::addBearerSecurityIfRequired);
        };
    }

    // ========================================================================
    // apiVersionHeaderCustomizer 헬퍼
    // ========================================================================

    private void addApiVersionHeaderIfRequired(final String path, final io.swagger.v3.oas.models.PathItem pathItem) {
        if (path == null || pathItem == null) {
            return;
        }
        if (!path.startsWith("/api/")) {
            return;
        }
        if (isVersionExemptPath(path)) {
            return;
        }
        pathItem.readOperations().forEach(this::addApiVersionParameter);
    }

    private boolean isVersionExemptPath(final String path) {
        return HEALTH_CHECK_PATH.equals(path)
                || SOCIAL_API_BASE_PATH.equals(path)
                || path.startsWith(SOCIAL_API_BASE_PATH + "/");
    }

    private void addApiVersionParameter(final io.swagger.v3.oas.models.Operation operation) {
        if (operation == null) {
            return;
        }

        final boolean alreadyAdded = operation.getParameters() != null
                && operation.getParameters().stream().anyMatch(p -> p != null
                && "header".equalsIgnoreCase(p.getIn())
                && ApiVersioning.HEADER_NAME.equalsIgnoreCase(p.getName()));

        if (alreadyAdded) {
            return;
        }

        operation.addParametersItem(new Parameter()
                .in("header")
                .name(ApiVersioning.HEADER_NAME)
                .required(true)
                .schema(new StringSchema())
                .description("API 버전 (예: 0.0, 1.0, 2.0). 미지정 시 기본값: " + ApiVersioning.DEFAULT_VERSION + " (유효하지 않음, 프론트에서 1.0 명시 필요)"));
    }

    // ========================================================================
    // bearerAuthCustomizer 헬퍼
    // ========================================================================

    private void addBearerSecurityIfRequired(final String path, final io.swagger.v3.oas.models.PathItem pathItem) {
        if (path == null || pathItem == null) {
            return;
        }
        if (!path.startsWith("/api/")) {
            return;
        }
        if (SecurityPublicPaths.isPublicApiPath(path)) {
            return;
        }
        pathItem.readOperations().forEach(this::addBearerSecurityItem);
    }

    private void addBearerSecurityItem(final io.swagger.v3.oas.models.Operation operation) {
        if (operation == null) {
            return;
        }

        final boolean alreadyAdded = operation.getSecurity() != null
                && operation.getSecurity().stream().anyMatch(requirement -> requirement != null
                && requirement.containsKey(BEARER_SECURITY_SCHEME));

        if (alreadyAdded) {
            return;
        }

        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_SECURITY_SCHEME));
    }
}
