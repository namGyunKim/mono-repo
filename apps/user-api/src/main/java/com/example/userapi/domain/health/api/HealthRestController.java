package com.example.userapi.domain.health.api;

import com.example.global.api.RestApiController;
import com.example.global.payload.response.RestApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UserHealthApi", description = "user-api 서버 상태 확인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthRestController {

    private final RestApiController restApiController;

    @Operation(summary = "user-api 서버 상태 확인")
    @GetMapping("/health")
    public ResponseEntity<RestApiResponse<String>> health() {
        return restApiController.ok("health");
    }
}
