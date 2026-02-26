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

    public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode, HttpStatus status) {
        return toResponse(errorCode, status, List.of());
    }

    public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode, HttpStatus status, List<ApiErrorDetail> errors) {
        ApiErrorResponse body = ApiErrorResponse.from(errorCode, errors);
        return restApiController.fail(body, status);
    }
}
