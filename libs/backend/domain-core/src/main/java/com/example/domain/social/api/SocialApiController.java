package com.example.domain.social.api;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.social.google.payload.dto.GoogleSocialRedirectCommand;
import com.example.domain.social.google.payload.request.GoogleRedirectRequest;
import com.example.domain.social.google.service.command.GoogleSocialLoginStartCommandService;
import com.example.domain.social.google.service.command.GoogleSocialRedirectCommandService;
import com.example.domain.social.google.service.query.GoogleSocialLoginStartQueryService;
import com.example.domain.social.google.validator.GoogleRedirectRequestValidator;
import com.example.domain.social.payload.response.SocialLoginSuccessResponse;
import com.example.domain.social.payload.response.SocialRedirectResponse;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.security.SecurityHeaders;
import com.example.global.security.TokenResponseHeaders;
import com.example.global.security.support.LocalTokenHeaderLoggingSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SocialApiController", description = "소셜 로그인 REST API")
@RestController
// 외부 OAuth Provider 콜백 특성상 API-Version 헤더 전달이 불가능하여, 이 컨트롤러는 헤더 버저닝 예외로 운영합니다.
// README의 API-Version 정책에 따라 /api/social/**는 헤더 예외로 취급합니다.
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialApiController {

    private final GoogleSocialLoginStartCommandService googleSocialLoginStartCommandService;
    private final GoogleSocialLoginStartQueryService googleSocialLoginStartQueryService;
    private final GoogleSocialRedirectCommandService googleSocialRedirectCommandService;
    private final GoogleRedirectRequestValidator googleRedirectRequestValidator;
    private final RestApiController restApiController;
    private final LocalTokenHeaderLoggingSupport localTokenHeaderLoggingSupport;

    @InitBinder("googleRedirectRequest")
    public void initGoogleRedirectBinder(WebDataBinder binder) {
        binder.addValidators(googleRedirectRequestValidator);
    }

    @Operation(summary = "구글 로그인 리다이렉트 URL 요청")
    @GetMapping(value = "/google/login")
    public ResponseEntity<RestApiResponse<SocialRedirectResponse>> googleLogin() {
        googleSocialLoginStartCommandService.prepareLoginSession();
        final String redirectUrl = googleSocialLoginStartQueryService.getRedirectUrl();
        final SocialRedirectResponse response = SocialRedirectResponse.of(redirectUrl);
        return restApiController.ok(response);
    }

    // 외부 OAuth Provider 콜백은 API-Version 헤더 전달이 불가능합니다.
    // 따라서 이 엔드포인트는 URL 버저닝(/v1/...)을 사용하며, 헤더 버저닝 예외로 취급합니다.
    @Operation(
            summary = "구글 로그인 콜백",
            description = """
                    토큰은 응답 헤더로 반환하고, 바디에는 성공 상태를 함께 반환합니다.
                    - Authorization: Bearer {accessToken}
                    - X-Refresh-Token: {refreshToken}
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = SocialLoginSuccessResponse.class)),
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
                    description = "인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping(value = "/v1/google/redirect")
    public ResponseEntity<RestApiResponse<SocialLoginSuccessResponse>> googleRedirect(
            @Valid @ModelAttribute("googleRedirectRequest") GoogleRedirectRequest googleRedirectRequest
    ) {
        final GoogleSocialRedirectCommand command = GoogleSocialRedirectCommand.from(googleRedirectRequest);
        final LoginTokenResponse response = googleSocialRedirectCommandService.loginByRedirect(command);
        localTokenHeaderLoggingSupport.logResponseTokenHeaders(
                "social-google-redirect",
                response.accessToken(),
                response.refreshToken()
        );
        final HttpHeaders headers = TokenResponseHeaders.of(response.accessToken(), response.refreshToken());
        return restApiController.okWithHeaders(SocialLoginSuccessResponse.ok(), headers);
    }
}
