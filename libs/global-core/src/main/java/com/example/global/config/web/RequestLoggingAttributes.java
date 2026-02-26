package com.example.global.config.web;

/**
 * 요청 수명주기 로깅(필터/컨트롤러 AOP)에서 사용하는 HttpServletRequest attribute 키 모음
 *
 * <p>
 * - 컨트롤러(@RestController) 레벨에서 이미 로깅된 요청인지 식별하여
 * 필터 레벨의 "fallback" 로깅이 중복으로 출력되지 않도록 합니다.
 * </p>
 */
public final class RequestLoggingAttributes {

    /**
     * {@code ControllerLoggingAspect}가 해당 요청을 로깅했음을 표시하는 attribute key
     */
    public static final String CONTROLLER_LOGGED = "__gyun_controller_logged__";

    /**
     * 보안 필터/서블릿 필터 레벨에서 이미 상세 로그를 남긴 요청인지 표시하는 attribute key
     *
     * <p>
     * 예: {@code JsonBodyLoginAuthenticationFilter}가 400 응답을 직접 생성하면서
     * 실패 원인(검증 errors 등)을 이미 로그로 남긴 경우, {@code FallbackRequestLoggingFilter}가
     * 같은 요청을 다시 요약 로그로 남기면 중복이 됩니다.
     * </p>
     */
    public static final String FILTER_LOGGED = "__gyun_filter_logged__";

    private RequestLoggingAttributes() {
    }
}
