package com.example.domain.member.api;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberDetailQuery;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.payload.request.MemberCreateRequest;
import com.example.domain.member.payload.request.MemberListRequest;
import com.example.domain.member.payload.request.MemberRoleUpdateRequest;
import com.example.domain.member.payload.request.MemberUpdateRequest;
import com.example.domain.member.payload.response.DetailMemberResponse;
import com.example.domain.member.payload.response.MemberListResponse;
import com.example.domain.member.service.MemberStrategyFactory;
import com.example.domain.member.service.command.MemberCommandService;
import com.example.domain.member.service.query.MemberQueryService;
import com.example.domain.member.validator.MemberCreateRequestPolicyValidator;
import com.example.domain.member.validator.MemberCreateValidator;
import com.example.domain.member.validator.MemberListRequestPolicyValidator;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.IdResponse;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.security.payload.SecurityLogoutCommand;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "AdminMemberApiController", description = "관리자 전용 회원 관리 REST API (전략 패턴 적용)")
@ConditionalOnProperty(name = "app.type", havingValue = "admin")
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminMemberApiController {

    private final MemberStrategyFactory memberStrategyFactory;

    private final MemberCreateValidator memberCreateValidator;
    private final MemberCreateRequestPolicyValidator memberCreateRequestPolicyValidator;
    private final MemberListRequestPolicyValidator memberListRequestPolicyValidator;

    private final RestApiController restApiController;
    private final AccessTokenResolver accessTokenResolver;

    @InitBinder
    public void initMemberCreateRequestBinder(WebDataBinder binder) {
        // @RequestBody 요청은 objectName 의존을 줄이기 위해 무인자 @InitBinder로 등록합니다.
        // 실제 적용 대상은 각 Validator의 supports(...) 타입 체크로 제한합니다.
        binder.addValidators(memberCreateValidator, memberCreateRequestPolicyValidator);
    }

    @InitBinder("memberListRequest")
    public void initMemberListRequestBinder(WebDataBinder binder) {
        binder.addValidators(memberListRequestPolicyValidator);
    }

    @Operation(summary = "회원 생성")
    @PostMapping(version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canManageRole(#memberCreateRequest.toDomainRole())")
    public ResponseEntity<RestApiResponse<IdResponse>> createMember(
            @Valid @RequestBody MemberCreateRequest memberCreateRequest
    ) {
        final MemberCommandService service = memberStrategyFactory.getCommandService(memberCreateRequest.toDomainRole());
        final Long createdId = service.createMember(MemberCreateCommand.from(memberCreateRequest));
        final IdResponse createResponse = IdResponse.of(createdId);

        return restApiController.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(createResponse.id())
                        .toUri(),
                createResponse
        );
    }

    @Operation(summary = "회원 목록 조회")
    @GetMapping(version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canManageRole(#memberListRequest.toDomainRole())")
    public ResponseEntity<RestApiResponse<Page<MemberListResponse>>> getMemberList(
            @Valid @ModelAttribute("memberListRequest") MemberListRequest memberListRequest
    ) {
        final MemberQueryService service = memberStrategyFactory.getQueryService(memberListRequest.toDomainRole());
        final Page<MemberListResponse> memberPage = service.getList(MemberListQuery.from(memberListRequest));

        return restApiController.ok(memberPage);
    }

    @Operation(summary = "회원 상세 조회")
    @GetMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<RestApiResponse<DetailMemberResponse>> getMemberDetail(
            @PathVariable Long id
    ) {
        final MemberQueryService service = memberStrategyFactory.getQueryServiceByMemberId(id);
        final DetailMemberResponse response = service.getDetail(MemberDetailQuery.of(id));

        return restApiController.ok(response);
    }

    @Operation(summary = "회원 정보 수정")
    @PutMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<RestApiResponse<IdResponse>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest memberUpdateRequest
    ) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        final Long updatedId = commandService.updateMember(
                MemberUpdateCommand.from(memberUpdateRequest, id)
        );
        final IdResponse updateResponse = IdResponse.of(updatedId);

        return restApiController.ok(updateResponse);
    }

    @Operation(summary = "회원 탈퇴/비활성화")
    @DeleteMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.hasAnyAdminRole() and @memberGuard.canAccessMember(#id)")
    public ResponseEntity<Void> deactivateMember(
            @PathVariable Long id,
            @CurrentAccount CurrentAccountDTO currentAccount,
            HttpServletRequest request
    ) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        final MemberDeactivateCommand deactivateCommand = accessTokenResolver.resolveAccessToken(request)
                .map(accessToken -> MemberDeactivateCommand.of(
                        id,
                        currentAccount.id(),
                        SecurityLogoutCommand.of(currentAccount.id(), accessToken)
                ))
                .orElseGet(() -> MemberDeactivateCommand.of(id, currentAccount.id()));
        commandService.deactivateMember(deactivateCommand);
        return restApiController.noContent();
    }

    @Operation(summary = "회원 등급 변경")
    @PatchMapping(value = "/{id}", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isSuperAdmin()")
    public ResponseEntity<RestApiResponse<IdResponse>> updateMemberRole(
            @PathVariable Long id,
            @Valid @RequestBody MemberRoleUpdateRequest memberRoleUpdateRequest
    ) {
        final MemberCommandService commandService = memberStrategyFactory.getCommandServiceByMemberId(id);
        commandService.updateMemberRole(MemberRoleUpdateCommand.from(id, memberRoleUpdateRequest));

        return restApiController.ok(IdResponse.of(id));
    }

}
