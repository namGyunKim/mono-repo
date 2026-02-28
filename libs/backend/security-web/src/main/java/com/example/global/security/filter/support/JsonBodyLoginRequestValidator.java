package com.example.global.security.filter.support;

import com.example.domain.account.validator.LoginAccountValidator;
import com.example.domain.account.validator.LoginRequestRoleStrategy;
import com.example.global.payload.response.ApiErrorDetail;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JsonBodyLoginRequestValidator {

    private static final String FALLBACK_LOGIN_OBJECT_NAME = "accountLoginRequest";

    private final Validator beanValidator;
    private final LoginAccountValidator loginAccountValidator;
    private final List<LoginRequestRoleStrategy> loginRequestRoleStrategies;

    @PostConstruct
    void validateDependencies() {
        if (loginRequestRoleStrategies == null || loginRequestRoleStrategies.isEmpty()) {
            throw new IllegalStateException("loginRequestRoleStrategies는 필수입니다.");
        }
    }

    public LoginRequestValidationResult validate(Object loginRequest) {
        final List<ApiErrorDetail> errors = new ArrayList<>();

        if (loginRequest == null) {
            errors.add(ApiErrorDetail.of("body", "요청 값이 비어있습니다."));
            return LoginRequestValidationResult.of(null, null, null, null, errors);
        }

        final LoginRequestRoleStrategy strategy = resolveStrategy(loginRequest);
        final String loginId = extractLoginId(strategy, loginRequest);
        final String password = extractPassword(strategy, loginRequest);

        // 1) Bean Validation
        final Set<ConstraintViolation<Object>> violations = beanValidator.validate(loginRequest);
        if (violations != null && !violations.isEmpty()) {
            for (final ConstraintViolation<Object> violation : violations) {
                final String field = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "";
                final String message = violation.getMessage() != null ? violation.getMessage() : "";
                errors.add(ApiErrorDetail.of(field, message));
            }
            // 필드 기본값이 누락된 상태에서는 DB 조회 기반 검증을 진행하지 않습니다.
            return LoginRequestValidationResult.of(loginRequest, strategy, loginId, password, errors);
        }

        // 2) 커스텀 Validator (계정 존재/상태/권한 정책 검증)
        final String objectName = resolveObjectName(loginRequest, strategy);
        final DataBinder dataBinder = new DataBinder(loginRequest, objectName);
        dataBinder.addValidators(loginAccountValidator);
        dataBinder.validate();
        final BindingResult bindingResult = dataBinder.getBindingResult();

        if (bindingResult.hasErrors()) {
            for (final FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.add(ApiErrorDetail.of(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : ""
                ));
            }

            for (final ObjectError objectError : bindingResult.getGlobalErrors()) {
                errors.add(ApiErrorDetail.of(
                        objectError.getObjectName(),
                        objectError.getDefaultMessage() != null ? objectError.getDefaultMessage() : ""
                ));
            }
        }

        return LoginRequestValidationResult.of(loginRequest, strategy, loginId, password, errors);
    }

    private String resolveObjectName(Object target, LoginRequestRoleStrategy strategy) {
        if (strategy != null) {
            final String objectName = strategy.resolveObjectName();
            if (objectName != null && !objectName.isBlank()) {
                return objectName;
            }
        }

        final String simpleName = target.getClass().getSimpleName();
        if (simpleName == null || simpleName.isBlank()) {
            return FALLBACK_LOGIN_OBJECT_NAME;
        }

        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    private LoginRequestRoleStrategy resolveStrategy(Object target) {
        if (target == null) {
            return null;
        }
        final Class<?> targetClass = target.getClass();
        return loginRequestRoleStrategies.stream()
                .filter(strategy -> strategy.supports(targetClass))
                .findFirst()
                .orElse(null);
    }

    private String extractLoginId(LoginRequestRoleStrategy strategy, Object loginRequest) {
        return strategy != null ? strategy.resolveLoginId(loginRequest) : null;
    }

    private String extractPassword(LoginRequestRoleStrategy strategy, Object loginRequest) {
        return strategy != null ? strategy.resolvePassword(loginRequest) : null;
    }
}
