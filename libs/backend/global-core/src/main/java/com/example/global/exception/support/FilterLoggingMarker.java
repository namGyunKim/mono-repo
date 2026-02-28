package com.example.global.exception.support;

import com.example.global.config.web.RequestLoggingAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class FilterLoggingMarker {

    public void markFilterLogged(final HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
    }
}
