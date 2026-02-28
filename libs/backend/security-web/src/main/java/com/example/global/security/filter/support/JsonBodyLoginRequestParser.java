package com.example.global.security.filter.support;

import com.example.domain.account.payload.request.AccountAdminLoginRequest;
import com.example.domain.account.payload.request.AccountUserLoginRequest;
import com.example.global.payload.response.ApiErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JsonBodyLoginRequestParser {

    /**
     * JSON 로그인 엔드포인트 (문서 기준)
     * <p>
     * - 일반 사용자: /api/sessions
     * - 관리자: /api/admin/sessions
     * </p>
     *
     * <p>
     * 실제 운영에서는 SecurityFilterChain에서 필터 매핑 URL(setFilterProcessesUrl)로 조정될 수 있으므로,
     * 여기서는 '관리자 경로인지'만 판별하고 그 외는 일반 로그인으로 처리합니다.
     * </p>
     */
    private static final String ADMIN_LOGIN_JSON_ENDPOINT = "/api/admin/sessions";

    private final ObjectMapper objectMapper;

    public boolean isJsonRequest(HttpServletRequest request) {
        final String contentType = request != null ? request.getContentType() : null;
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE);
    }

    public LoginRequestParseResult parse(HttpServletRequest request) {
        if (request == null) {
            return LoginRequestParseResult.failure(
                    List.of(ApiErrorDetail.of("body", "요청 값이 비어있습니다."))
            );
        }

        final String path = getPathWithinApplication(request);

        try (InputStream inputStream = request.getInputStream()) {
            if (isAdminLoginPath(path)) {
                return LoginRequestParseResult.of(objectMapper.readValue(inputStream, AccountAdminLoginRequest.class));
            }
            return LoginRequestParseResult.of(objectMapper.readValue(inputStream, AccountUserLoginRequest.class));
        } catch (JacksonException e) {
            return LoginRequestParseResult.from(
                    List.of(ApiErrorDetail.of("body", "요청 본문(JSON) 파싱에 실패했습니다."))
            );
        } catch (IOException e) {
            return LoginRequestParseResult.from(
                    List.of(ApiErrorDetail.of("body", "요청 본문을 읽는 중 오류가 발생했습니다."))
            );
        }
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        if (uri == null) {
            return "";
        }

        final String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }

    private boolean isAdminLoginPath(String path) {
        if (path == null) {
            return false;
        }

        // 문서 기준 엔드포인트
        if (path.equals(ADMIN_LOGIN_JSON_ENDPOINT) || path.startsWith(ADMIN_LOGIN_JSON_ENDPOINT + "/")) {
            return true;
        }

        // 혹시 contextPath/서블릿 설정 등에 의해 전체 경로로 매칭될 수 있으므로, suffix도 허용합니다.
        return path.endsWith(ADMIN_LOGIN_JSON_ENDPOINT);
    }
}
