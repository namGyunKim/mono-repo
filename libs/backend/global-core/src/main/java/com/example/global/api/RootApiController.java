package com.example.global.api;

import com.example.global.payload.response.RestApiResponse;
import com.example.global.payload.response.RootInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 루트("/") / favicon 요청 처리
 *
 * <p>
 * REST API 프로젝트에서 브라우저로 서버 주소에 접근하면 기본적으로
 * "/" 및 "/favicon.ico" 요청이 발생합니다.
 *
 * <ul>
 *   <li>"/" : 매핑이 없으면 정적 리소스 탐색 → NoResourceFoundException 경고 로그</li>
 *   <li>"/favicon.ico" : 파비콘 파일이 없으면 동일 경고 로그</li>
 * </ul>
 * <p>
 * 위 로그를 줄이기 위해 최소 응답을 제공하는 컨트롤러입니다.
 * </p>
 */
@PreAuthorize("permitAll()")
@RestController
@RequiredArgsConstructor
public class RootApiController {

    private final RestApiController restApiController;

    @Value("${springdoc.swagger-ui.enabled:false}")
    private boolean swaggerUiEnabled;

    @Operation(summary = "루트 정보 조회")
    @GetMapping("/")
    public ResponseEntity<RestApiResponse<RootInfoResponse>> root() {
        return restApiController.ok(RootInfoResponse.of(swaggerUiEnabled));
    }

    @Operation(summary = "파비콘 요청 처리")
    @RequestMapping({"/favicon.ico", "/favicon.svg"})
    public ResponseEntity<Void> favicon() {
        // REST API 서버에서는 파비콘이 필수가 아니므로, 204로 조용히 응답합니다.
        return restApiController.noContent();
    }
}
