package com.example.global.security.handler.support;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoginFailureResponseWriter {

    private final ObjectMapper objectMapper;

    public void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode,
            List<ApiErrorDetail> errors
    ) throws IOException {
        if (response == null || response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        final ApiErrorResponse body = ApiErrorResponse.from(errorCode, errors);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
