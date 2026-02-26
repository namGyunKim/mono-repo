package com.example.global.config.web.support;

import com.example.domain.security.guard.MemberGuard;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FallbackLoginIdResolver {

    private static final String DEFAULT_GUEST = "GUEST";
    private static final String PARAM_LOGIN_ID = "loginId";

    private final MemberGuard memberGuard;

    public String resolveLoginId(HttpServletRequest request) {
        if (request == null) {
            return DEFAULT_GUEST;
        }

        String paramLoginId = request.getParameter(PARAM_LOGIN_ID);
        if (hasText(paramLoginId)) {
            return paramLoginId.trim();
        }

        Object attribute = request.getAttribute(JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID);
        if (attribute instanceof String attrLoginId && hasText(attrLoginId)) {
            return attrLoginId.trim();
        }

        return memberGuard.getLoginIdOrDefault(DEFAULT_GUEST);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
