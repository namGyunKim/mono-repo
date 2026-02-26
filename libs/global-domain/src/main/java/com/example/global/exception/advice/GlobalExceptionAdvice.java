package com.example.global.exception.advice;

import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ApiErrorResponseFactory;
import com.example.global.exception.support.ApiVersionErrorResolver;
import com.example.global.exception.support.HttpStatusResolver;
import com.example.global.exception.support.ValidationErrorMapper;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.accept.NotAcceptableApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionAdvice {

    private final ApiErrorResponseFactory apiErrorResponseFactory;
    private final HttpStatusResolver httpStatusResolver;
    private final ValidationErrorMapper validationErrorMapper;
    private final ApiVersionErrorResolver apiVersionErrorResolver;

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseAppException(BaseAppException exception) {
        HttpStatus status = httpStatusResolver.resolve(exception.getErrorCode());
        return apiErrorResponseFactory.toResponse(exception.getErrorCode(), status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException() {
        return apiErrorResponseFactory.toResponse(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidParameterException() {
        return apiErrorResponseFactory.toResponse(ErrorCode.INVALID_PARAMETER, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception exception) {
        return apiErrorResponseFactory.toResponse(
                ErrorCode.INPUT_VALUE_INVALID,
                HttpStatus.BAD_REQUEST,
                validationErrorMapper.resolveValidationErrors(exception)
        );
    }

    @ExceptionHandler(MissingApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingApiVersionException() {
        return apiErrorResponseFactory.toResponse(ErrorCode.API_VERSION_REQUIRED, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotAcceptableApiVersionException.class, InvalidApiVersionException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidApiVersionException(HttpServletRequest request) {
        ErrorCode resolved = apiVersionErrorResolver.resolve(request, ErrorCode.API_VERSION_INVALID);
        return apiErrorResponseFactory.toResponse(resolved, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(HttpServletRequest request) {
        return toNotFoundResponse(request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(HttpServletRequest request) {
        return toNotFoundResponse(request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupportedException() {
        return apiErrorResponseFactory.toResponse(ErrorCode.METHOD_NOT_SUPPORTED, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnhandledException() {
        return apiErrorResponseFactory.toResponse(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> toNotFoundResponse(HttpServletRequest request) {
        ErrorCode resolved = apiVersionErrorResolver.resolve(request, ErrorCode.PAGE_NOT_EXIST);
        HttpStatus status = httpStatusResolver.resolve(resolved);
        return apiErrorResponseFactory.toResponse(resolved, status);
    }
}
