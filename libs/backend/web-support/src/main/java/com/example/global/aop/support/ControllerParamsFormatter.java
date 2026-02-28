package com.example.global.aop.support;

import com.example.domain.security.PrincipalDetails;
import com.example.global.utils.LoggingSanitizerPolicy;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerParamsFormatter {

    private static final int MAX_LOG_LENGTH = 2048;
    private static final String TRUNCATED_SUFFIX = "...";

    private final ObjectMapper objectMapper;

    /**
     * 메서드 파라미터를 Map으로 변환하여 JSON 문자열로 반환
     * - 불필요한 객체(HttpServletRequest 등)는 제외
     * - LinkedHashMap을 사용하여 파라미터 순서 보장
     */
    public String formatParams(ProceedingJoinPoint joinPoint) {
        final Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "파라미터 없음";
        }

        try {
            // 순서를 보장하기 위해 LinkedHashMap 사용
            final Map<String, Object> loggableArgs = new LinkedHashMap<>();
            final String[] parameterNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                String paramName = parameterNames != null ? parameterNames[i] : "arg" + i;

                // 로깅 가능한 타입만 맵에 담기
                if (isLoggable(arg)) {
                    loggableArgs.put(paramName, sanitize(paramName, arg));
                }
            }

            if (loggableArgs.isEmpty()) {
                return "없음(필터링됨)";
            }

            // JSON Pretty Print 적용 (줄바꿈 및 들여쓰기)
            final String formatted = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(loggableArgs);
            return truncateLog(formatted);

        } catch (Exception e) {
            log.warn(
                    "traceId={}, 요청 파라미터 로깅 실패: {}",
                    TraceIdUtils.resolveTraceId(),
                    e.getClass().getSimpleName()
            );
            return "파라미터 파싱 실패";
        }
    }

    /**
     * 로깅에서 제외할 타입 필터링
     * DataBinder(WebDataBinder)는 순환 참조 에러를 유발하므로 제외
     */
    private boolean isLoggable(Object arg) {
        return arg != null &&
                !(arg instanceof HttpServletRequest) &&
                !(arg instanceof HttpServletResponse) &&
                !(arg instanceof BindingResult) &&
                !(arg instanceof DataBinder) && // WebDataBinder 포함
                !(arg instanceof PrincipalDetails) &&
                !(arg instanceof MultipartFile) &&
                !(arg instanceof MultipartFile[]) &&
                (isSimpleValue(arg) || isSanitizable(arg));
    }

    /**
     * 요청 파라미터 로깅 시 민감 필드를 마스킹합니다.
     * - DTO/Record 형태 요청 객체에 비밀번호/토큰이 포함될 수 있으므로 보호 처리
     */
    private Object sanitize(String paramName, Object arg) {
        if (arg == null) {
            return null;
        }

        // 단순 타입(String/Number/Boolean/Enum)이라도 파라미터명이 민감 키면 값 자체를 마스킹합니다.
        if (isSensitiveField(paramName)) {
            return "***";
        }

        if (isSimpleValue(arg)) {
            return arg;
        }

        if (!isSanitizable(arg)) {
            return "UNSUPPORTED(" + arg.getClass().getSimpleName() + ")";
        }

        try {
            final JsonNode node = objectMapper.valueToTree(arg);
            maskSensitive(node);
            return node;
        } catch (Exception e) {
            // 변환 실패 시 원본 노출을 피하기 위해 마스킹 처리
            return "***";
        }
    }

    private boolean isSanitizable(Object arg) {
        if (arg == null) {
            return false;
        }

        if (arg instanceof Record) {
            return true;
        }

        return arg instanceof Map<?, ?>
                || arg instanceof Iterable<?>
                || arg.getClass().isArray();
    }

    private boolean isSimpleValue(Object arg) {
        return arg instanceof String
                || arg instanceof Number
                || arg instanceof Boolean
                || arg.getClass().isEnum();
    }

    private void maskSensitive(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            // Jackson 3: fieldNames() 제거 → propertyNames() 사용
            // propertyNames()는 Collection<String>을 반환하므로 snapshot을 떠서 안전하게 순회합니다.
            for (String fieldName : List.copyOf(obj.propertyNames())) {
                JsonNode child = obj.get(fieldName);

                if (isSensitiveField(fieldName)) {
                    obj.put(fieldName, "***");
                    continue;
                }

                maskSensitive(child);
            }
            return;
        }

        if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            for (JsonNode child : arr) {
                maskSensitive(child);
            }
        }
    }

    private boolean isSensitiveField(String fieldName) {
        return LoggingSanitizerPolicy.isSensitiveField(fieldName);
    }

    private String truncateLog(String logMessage) {
        if (logMessage == null) {
            return null;
        }

        if (logMessage.length() <= MAX_LOG_LENGTH) {
            return logMessage;
        }

        return logMessage.substring(0, MAX_LOG_LENGTH) + TRUNCATED_SUFFIX;
    }
}
