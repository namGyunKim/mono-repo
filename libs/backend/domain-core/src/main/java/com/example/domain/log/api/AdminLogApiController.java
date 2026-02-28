package com.example.domain.log.api;

import com.example.domain.log.payload.dto.MemberLogQuery;
import com.example.domain.log.payload.request.MemberLogRequest;
import com.example.domain.log.payload.response.MemberLogResponse;
import com.example.domain.log.service.query.MemberLogQueryService;
import com.example.domain.log.validator.MemberLogRequestPolicyValidator;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminLogApiController", description = "관리자 전용 시스템 로그 REST API")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("@memberGuard.hasAnyAdminRole()")
public class AdminLogApiController {

    private final MemberLogQueryService memberLogQueryService;
    private final MemberLogRequestPolicyValidator memberLogRequestPolicyValidator;
    private final RestApiController restApiController;

    @InitBinder("memberLogRequest")
    public void initMemberLogRequestBinder(WebDataBinder binder) {
        binder.addValidators(memberLogRequestPolicyValidator);
    }

    @Operation(summary = "회원 활동 로그 목록 조회")
    @GetMapping(value = "/members", version = ApiVersioning.V1)
    public ResponseEntity<RestApiResponse<Page<MemberLogResponse>>> memberLogList(
            @Valid @ModelAttribute("memberLogRequest") MemberLogRequest memberLogRequest
    ) {
        Page<MemberLogResponse> logPage = memberLogQueryService.getMemberLogs(MemberLogQuery.from(memberLogRequest));
        return restApiController.ok(logPage);
    }

}
