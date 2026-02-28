package com.example.global.exception.support;

import com.example.global.api.RestApiController;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApiErrorResponseFactoryTest {

    @Mock
    private RestApiController restApiController;

    @InjectMocks
    private ApiErrorResponseFactory factory;

    @Test
    void toResponse_without_errors_delegates_correctly() {
        final ResponseEntity<ApiErrorResponse> expected = ResponseEntity.badRequest().build();
        given(restApiController.fail(any(ApiErrorResponse.class), eq(HttpStatus.BAD_REQUEST)))
                .willReturn(expected);

        final ResponseEntity<ApiErrorResponse> result = factory.toResponse(ErrorCode.INPUT_VALUE_INVALID, HttpStatus.BAD_REQUEST);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void toResponse_with_errors_delegates_correctly() {
        final List<ApiErrorDetail> errors = List.of(ApiErrorDetail.of("field", "reason"));
        final ResponseEntity<ApiErrorResponse> expected = ResponseEntity.badRequest().build();
        given(restApiController.fail(any(ApiErrorResponse.class), eq(HttpStatus.BAD_REQUEST)))
                .willReturn(expected);

        final ResponseEntity<ApiErrorResponse> result = factory.toResponse(ErrorCode.INPUT_VALUE_INVALID, HttpStatus.BAD_REQUEST, errors);
        assertThat(result).isEqualTo(expected);
    }
}
