package com.example.global.exception.support;

import com.example.global.payload.response.ApiErrorDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

@Component
public class ValidationErrorMapper {

    public List<ApiErrorDetail> resolveValidationErrors(Exception e) {
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            bindingResult = methodArgumentNotValidException.getBindingResult();
        } else if (e instanceof BindException bindException) {
            bindingResult = bindException.getBindingResult();
        }
        if (bindingResult == null) {
            return List.of();
        }

        return bindingResult.getAllErrors().stream()
                .map(this::toApiErrorDetail)
                .toList();
    }

    public String resolveValidationDetailMessage(Exception e, String fallback) {
        BindingResult bindingResult = null;
        if (e instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            bindingResult = methodArgumentNotValidException.getBindingResult();
        } else if (e instanceof BindException bindException) {
            bindingResult = bindException.getBindingResult();
        }
        if (bindingResult == null) {
            return fallback;
        }

        List<ApiErrorDetail> details = bindingResult.getAllErrors().stream()
                .map(this::toApiErrorDetail)
                .toList();
        if (details.isEmpty()) {
            return fallback;
        }

        String joinedDetails = details.stream()
                .map(detail -> "%s=%s".formatted(detail.field(), detail.reason()))
                .reduce((left, right) -> left + ", " + right)
                .orElse(fallback);

        return "요청 값 검증 실패: %s".formatted(joinedDetails);
    }

    private ApiErrorDetail toApiErrorDetail(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return ApiErrorDetail.of(fieldError.getField(), resolveErrorMessage(fieldError));
        }
        return ApiErrorDetail.of(error.getObjectName(), resolveErrorMessage(error));
    }

    private String resolveErrorMessage(ObjectError error) {
        if (error == null) {
            return "";
        }
        String message = error.getDefaultMessage();
        return message != null ? message : "";
    }
}
