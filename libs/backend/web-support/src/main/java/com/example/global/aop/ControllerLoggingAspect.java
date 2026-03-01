package com.example.global.aop;

import com.example.domain.security.guard.MemberGuard;
import com.example.global.aop.support.ControllerLoggingSupport;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 컨트롤러 요청/응답 로깅 Aspect
 * - 모든 컨트롤러 메서드의 진입/종료 시점에 로그를 남깁니다.
 * - 요청 파라미터를 JSON 형태로 변환하여 변수명과 함께 출력합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final ControllerLoggingSupport loggingSupport;
    private final MemberGuard memberGuard;

    /**
     * 포인트컷: @RestController 에만 적용
     *
     * <p>
     * 클래스명 규칙(*Controller)만으로 포인트컷을 잡으면, HTTP 엔드포인트가 아닌 컴포넌트(예: RestApiController)까지
     * 중복 로깅되는 문제가 발생할 수 있습니다.
     * 따라서 실제 요청을 처리하는 @RestController 만 대상으로 한정합니다.
     * </p>
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    private void restControllerMethods() {
    }

    @Around("restControllerMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // RequestAttributes가 없으면(테스트 환경 등) 바로 진행
        if (RequestContextHolder.getRequestAttributes() == null) {
            return joinPoint.proceed();
        }

        final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // Filter 레벨의 fallback 로깅과 중복되지 않도록 "컨트롤러에서 로깅 완료" 플래그를 남깁니다.
        request.setAttribute(RequestLoggingAttributes.CONTROLLER_LOGGED, Boolean.TRUE);

        final String traceId = logRequest(request, joinPoint);

        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            stopWatch.stop();
            logResponse(traceId, stopWatch.getTotalTimeMillis());
        }

        return result;
    }

    /**
     * 요청 정보를 수집하여 [REQ] 로그를 출력합니다.
     *
     * @return traceId (응답 로깅에서 재사용)
     */
    private String logRequest(final HttpServletRequest request, final ProceedingJoinPoint joinPoint) {
        final String traceId = TraceIdUtils.resolveTraceId();
        final String ip = ClientIpExtractor.extract(request);
        final String method = request.getMethod();
        final String uri = request.getRequestURI();
        final String loginId = getLoginIdFromSecurityContext();
        final String params = loggingSupport.formatParams(joinPoint);

        log.info(loggingSupport.buildRequestLog(traceId, ip, loginId, method, uri, params));
        return traceId;
    }

    /**
     * [RES] 응답 로그를 출력합니다.
     */
    private void logResponse(final String traceId, final long elapsedMillis) {
        try {
            final HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            final String responseStatus = loggingSupport.getResponseStatus(response);
            final String responseSize = loggingSupport.getResponseSize(response);
            log.info(loggingSupport.buildResponseLog(traceId, elapsedMillis, responseStatus, responseSize));
        } catch (final IllegalStateException e) {
            log.warn("응답 로깅 스킵: RequestAttributes 소실 time={}ms", elapsedMillis);
        }
    }

    /**
     * SecurityContext에서 로그인 사용자 ID 추출
     */
    private String getLoginIdFromSecurityContext() {
        return memberGuard.getLoginIdOrDefault("GUEST");
    }
}
