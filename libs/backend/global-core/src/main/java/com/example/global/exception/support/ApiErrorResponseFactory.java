package com.example.global.exception.support;

import com.example.global.api.RestApiController;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiErrorResponseFactory {

    private final RestApiController restApiController;

    public ResponseEntity<ApiErrorResponse> toResponse(final ErrorCode errorCode, final HttpStatus status) {
        return toResponse(errorCode, status, List.of());
    }

    public ResponseEntity<ApiErrorResponse> toResponse(final ErrorCode errorCode, final HttpStatus status, final List<ApiErrorDetail> errors) {
        final ApiErrorResponse body = ApiErrorResponse.from(errorCode, errors);
        return restApiController.fail(body, status);
    }
}
