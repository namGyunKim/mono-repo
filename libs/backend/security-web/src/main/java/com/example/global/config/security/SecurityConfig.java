package com.example.global.config.security;

import com.example.global.security.SecurityHeaders;
import com.example.global.security.SecurityPublicPaths;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import com.example.global.security.filter.JwtAuthenticationFilter;
import com.example.global.security.filter.support.JsonBodyLoginErrorWriter;
import com.example.global.security.filter.support.JsonBodyLoginRequestParser;
import com.example.global.security.filter.support.JsonBodyLoginRequestValidator;
import com.example.global.security.handler.CustomAccessDeniedHandler;
import com.example.global.security.handler.CustomAuthFailureHandler;
import com.example.global.security.handler.CustomAuthSuccessHandler;
import com.example.global.security.handler.CustomAuthenticationEntryPoint;
import com.example.global.security.jwt.JwtProperties;
import com.example.global.security.service.query.PrincipalDetailsQueryService;
import com.example.global.utils.RequestUriUtils;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * JSON 로그인 API 경로
     * <p>
     * - Swagger/Postman 등 비브라우저 클라이언트에서 JSON 바디 인증을 허용합니다.
     */
    private static final String AUTH_LOGIN_JSON_API_PATH = "/api/sessions";
    private static final String ADMIN_LOGIN_JSON_API_PATH = "/api/admin/sessions";

    @Value("${app.cors.allowed-origins:}")
    private List<String> allowedOrigins;

    private static boolean isJsonLoginApiRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String path = RequestUriUtils.getPathWithinApplication(request);
        return path.equals(AUTH_LOGIN_JSON_API_PATH) || path.equals(ADMIN_LOGIN_JSON_API_PATH);
    }

    private static boolean isJsonLoginApiAuthenticationRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        final String method = request.getMethod();
        if (method == null || !"POST".equalsIgnoreCase(method)) {
            return false;
        }

        return isJsonLoginApiRequest(request);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            PrincipalDetailsQueryService principalDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(principalDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DaoAuthenticationProvider daoAuthenticationProvider,
            CustomAuthSuccessHandler successHandler, CustomAuthFailureHandler failureHandler,
            CustomAccessDeniedHandler accessDeniedHandler, CustomAuthenticationEntryPoint entryPoint,
            JsonBodyLoginRequestParser parser, JsonBodyLoginRequestValidator validator,
            JsonBodyLoginErrorWriter errorWriter, AuthenticationConfiguration authConfig,
            JwtAuthenticationFilter jwtFilter
    ) throws Exception {
        configureBasePolicy(http);

        final var loginFilter = createJsonBodyLoginFilter(parser, validator, errorWriter, authConfig, successHandler, failureHandler);
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(entryPoint));
        http.authenticationProvider(daoAuthenticationProvider);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    private void configureBasePolicy(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(SecurityPublicPaths.PUBLIC_URLS).permitAll()
                .requestMatchers(SecurityPublicPaths.PUBLIC_API_URLS).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
        );
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private JsonBodyLoginAuthenticationFilter createJsonBodyLoginFilter(
            JsonBodyLoginRequestParser parser,
            JsonBodyLoginRequestValidator validator,
            JsonBodyLoginErrorWriter errorWriter,
            AuthenticationConfiguration authConfig,
            CustomAuthSuccessHandler successHandler,
            CustomAuthFailureHandler failureHandler
    ) throws Exception {
        final JsonBodyLoginAuthenticationFilter filter = new JsonBodyLoginAuthenticationFilter(parser, validator, errorWriter);
        filter.setAuthenticationManager(authConfig.getAuthenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        filter.setRequiresAuthenticationRequestMatcher(SecurityConfig::isJsonLoginApiAuthenticationRequest);
        return filter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        final List<String> normalizedOrigins = allowedOrigins == null ? List.of() : allowedOrigins.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // 기본적으로는 same-origin이므로, 필요한 경우에만 Origin을 등록하세요.
        configuration.setAllowedOrigins(normalizedOrigins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(
                SecurityHeaders.AUTHORIZATION,
                SecurityHeaders.REFRESH_TOKEN,
                TraceIdUtils.resolveTraceHeaderName()
        ));
        configuration.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
