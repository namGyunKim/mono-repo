package com.example.domain.account.api;

import com.example.domain.account.payload.request.AccountAdminLoginRequest;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.SecurityHeaders;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 로그인 엔드포인트는 Spring Security 인증 필터에서 처리되므로,
 * OpenAPI/Swagger 문서 노출을 위한 전용 컨트롤러를 제공합니다.
 *
 * <p>
 * - 실제 인증 로직은 JsonBodyLoginAuthenticationFilter(또는 formLogin 필터)에서 수행됩니다.
 * - 본 컨트롤러는 "문서 스키마 제공" 목적이며, 정상적인 구성에서는 호출되지 않습니다.
 * </p>
 */
@Tag(name = "Auth", description = "관리자 로그인 API (Spring Security Filter 기반)")
@PreAuthorize("permitAll()")
@ConditionalOnProperty(name = "app.type", havingValue = "admin")
@RestController
@RequiredArgsConstructor
public class AdminAccountAuthDocsApiController {

    private final RestApiController restApiController;

    @Operation(
            summary = "관리자 로그인 (JSON 바디)",
            description = """
                    Spring Security 인증 필터에서 처리됩니다.
                    요청 바디(JSON)로 loginId/password를 전달합니다. (서버에서 ADMIN/SUPER_ADMIN 권한 로그인만 허용)
                    응답 바디 없이 토큰은 응답 헤더로만 반환합니다.
                    - 응답 헤더:
                      - Authorization: Bearer {accessToken}
                      - X-Refresh-Token: {refreshToken}
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "로그인 성공",
                    headers = {
                            @Header(
                                    name = SecurityHeaders.AUTHORIZATION,
                                    description = "Bearer 접두사가 포함된 액세스 토큰",
                                    schema = @Schema(type = "string", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                            ),
                            @Header(
                                    name = SecurityHeaders.REFRESH_TOKEN,
                                    description = "Bearer 접두사가 없는 리프레시 토큰",
                                    schema = @Schema(type = "string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(아이디/비밀번호 불일치 등)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping(
            value = "/api/admin/sessions",
            version = ApiVersioning.V1,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> adminLoginJson(@Valid @RequestBody AccountAdminLoginRequest accountAdminLoginRequest) {
        // 문서 노출용 Stub (실제 호출 시도는 Filter가 선처리합니다.)
        return restApiController.noContent();
    }
}
