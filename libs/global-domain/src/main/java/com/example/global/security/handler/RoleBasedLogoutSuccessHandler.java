package com.example.global.security.handler;

import com.example.domain.account.payload.response.LogoutResponse;
import com.example.global.payload.response.RestApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 로그아웃 성공 시 JSON 응답을 반환합니다.
 */
@Component
@RequiredArgsConstructor
public class RoleBasedLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        LogoutResponse logoutResponse = LogoutResponse.of("로그아웃 성공");
        RestApiResponse<LogoutResponse> body = RestApiResponse.success(logoutResponse);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
