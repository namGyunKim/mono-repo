package com.example.global.api;

import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.payload.response.RestApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * 공통 REST 응답 생성(매핑) 컴포넌트
 *
 * <p>
 * - Controller에서 ResponseEntity 생성 로직이 중복되는 것을 방지합니다.
 * - "createXxx" 형태 메서드가 실제로는 매핑/래핑 성격이므로,
 * 표준 메서드명(fail/ok/created)으로 정리합니다.
 * </p>
 */
@Component
public class RestApiController {

    // ========================================================================
    // 표준 메서드 (권장)
    // ========================================================================

    /**
     * 실패 응답 (기본 UNAUTHORIZED)
     */
    public ResponseEntity<ApiErrorResponse> fail(ApiErrorResponse error) {
        return fail(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 실패 응답 (Status Code 지정)
     */
    public ResponseEntity<ApiErrorResponse> fail(ApiErrorResponse error, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(error);
    }

    /**
     * JWT 에러용 실패 응답(기본 UNAUTHORIZED)
     * <p>
     * - 현재는 fail(..., UNAUTHORIZED)와 동일하지만,
     * 향후 JWT 전용 payload/헤더 정책이 생길 수 있어 메서드를 분리해둡니다.
     * </p>
     */
    public ResponseEntity<ApiErrorResponse> failWithJwt(ApiErrorResponse error) {
        return fail(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 성공 응답 (OK 200)
     */
    public <T> ResponseEntity<RestApiResponse<T>> ok(T data) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(RestApiResponse.success(data));
    }

    /**
     * 성공 응답 (OK 200 + Headers)
     */
    public <T> ResponseEntity<RestApiResponse<T>> okWithHeaders(T data, HttpHeaders headers) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(resolveHeaders(headers))
                .body(RestApiResponse.success(data));
    }


    /**
     * 응답 바디가 필요 없는 경우 (NO_CONTENT 204)
     */
    public ResponseEntity<Void> noContent() {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * 응답 바디가 필요 없고 헤더를 포함해야 하는 경우 (NO_CONTENT 204 + Headers)
     */
    public ResponseEntity<Void> noContentWithHeaders(HttpHeaders headers) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .headers(resolveHeaders(headers))
                .build();
    }

    /**
     * 생성 성공 응답 (CREATED 201)
     */
    public <T> ResponseEntity<RestApiResponse<T>> created(T data) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RestApiResponse.success(data));
    }

    /**
     * 생성 성공 응답 (CREATED 201 + Location)
     */
    public <T> ResponseEntity<RestApiResponse<T>> created(URI location, T data) {
        return ResponseEntity
                .created(location)
                .body(RestApiResponse.success(data));
    }

    private HttpHeaders resolveHeaders(HttpHeaders headers) {
        final HttpHeaders responseHeaders = new HttpHeaders();
        if (headers != null && !headers.isEmpty()) {
            responseHeaders.putAll(headers);
        }
        return responseHeaders;
    }

}
