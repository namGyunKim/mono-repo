package com.example.domain.account.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Swagger/OpenAPI 문서에서 리프레시 토큰 갱신 성공 응답을 명확히 보여주기 위한 문서 전용 DTO
 *
 * <p>
 * - 실제 런타임 응답은 RestApiResponse<Void> 형태로 내려가며,
 * 토큰은 응답 헤더로 전달됩니다.
 * - OpenAPI UI에서 제네릭이 깨끗하게 표현되지 않는 경우가 있어 문서 전용 Wrapper를 제공합니다.
 * </p>
 */
@Schema(name = "RefreshTokenSuccessResponse", description = "리프레시 토큰 갱신 성공 응답")
public record RefreshTokenSuccessResponseDoc(
        @Schema(description = "응답 데이터(토큰은 헤더로 전달)")
        RefreshTokenResponse data
) {

    public static RefreshTokenSuccessResponseDoc of(RefreshTokenResponse data) {
        return new RefreshTokenSuccessResponseDoc(data);
    }
}
