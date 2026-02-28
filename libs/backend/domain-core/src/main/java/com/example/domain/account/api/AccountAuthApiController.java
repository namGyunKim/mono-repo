package com.example.domain.account.api;

import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.account.support.AccountTokenRefreshPort;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.SecurityHeaders;
import com.example.global.security.TokenResponseHeaders;
import com.example.global.security.support.LocalTokenHeaderLoggingSupport;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "JWT 토큰 갱신 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tokens")
@Validated
public class AccountAuthApiController {

    private final AccountTokenRefreshPort accountTokenRefreshPort;
    private final RestApiController restApiController;
    private final LocalTokenHeaderLoggingSupport localTokenHeaderLoggingSupport;

    @Operation(
            summary = "리프레시 토큰으로 액세스 토큰 갱신",
            description = """
                    리프레시 토큰을 검증한 뒤, 신규 Access/Refresh 토큰을 발급합니다.
                    - 본 API는 만료된 AccessToken 상태에서도 호출되어야 하므로 공개 API로 유지합니다.
                    - 요청 헤더: X-Refresh-Token: {refreshToken}
                    - 응답 헤더:
                      - Authorization: Bearer {accessToken}
                      - X-Refresh-Token: {refreshToken}
                    - 로그인/리프레시 모두 토큰은 응답 헤더로만 전달합니다.
                    - 기존 리프레시 토큰은 즉시 폐기(블랙리스트)되어 재사용이 차단됩니다.
                    - 에러 코드:
                      - 400: INPUT_VALUE_INVALID(0002), INVALID_PARAMETER(0003)
                      - 401: AUTHENTICATION_FAILED(1002)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "갱신 성공",
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
                    description = "요청 값 검증 실패 (INPUT_VALUE_INVALID=0002, INVALID_PARAMETER=0003)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패(리프레시 토큰 유효하지 않음/만료됨 등, AUTHENTICATION_FAILED=1002)",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping(
            version = ApiVersioning.V1,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> refresh(
            @Parameter(
                    description = "리프레시 토큰 헤더(Bearer 접두사 없이 전달)",
                    required = true,
                    in = ParameterIn.HEADER,
                    name = SecurityHeaders.REFRESH_TOKEN,
                    example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
            @RequestHeader(SecurityHeaders.REFRESH_TOKEN)
            @NotBlank(message = "refreshToken은 필수입니다.")
            String refreshToken
    ) {
        RefreshTokenResponse response = accountTokenRefreshPort.refreshTokens(refreshToken);
        localTokenHeaderLoggingSupport.logResponseTokenHeaders(
                "refresh",
                response.accessToken(),
                response.refreshToken()
        );
        HttpHeaders headers = TokenResponseHeaders.of(response.accessToken(), response.refreshToken());
        return restApiController.noContentWithHeaders(headers);
    }
}
