package com.example.global.config.web.support;

import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ApiVersionErrorResponder {

    private final ObjectMapper objectMapper;

    public void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {
        if (response == null || response.isCommitted() || errorCode == null) {
            return;
        }

        markFilterLogged(request);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        ApiErrorResponse body = ApiErrorResponse.from(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void markFilterLogged(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
    }
}
