package com.example.domain.account.api;

import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.payload.dto.AccountWithdrawCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.account.payload.request.AccountProfileUpdateRequest;
import com.example.domain.account.payload.response.LoginMemberResponse;
import com.example.domain.account.service.command.AccountCommandService;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.payload.response.IdResponse;
import com.example.global.payload.response.RestApiResponse;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AccountApiController", description = "계정 관련 REST API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountQueryService queryAccountService;
    private final AccountCommandService accountCommandService;
    private final RestApiController restApiController;
    private final AccessTokenResolver accessTokenResolver;

    @Operation(summary = "내 프로필 조회")
    @GetMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated()")
    public ResponseEntity<RestApiResponse<LoginMemberResponse>> profile(@CurrentAccount CurrentAccountDTO currentAccount) {
        LoginMemberView view = queryAccountService.getLoginData(currentAccount);
        LoginMemberResponse response = LoginMemberResponse.from(view);
        return restApiController.ok(response);
    }

    @Operation(summary = "내 프로필 수정")
    @PutMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated() and @memberGuard.canAccessSelf(#currentAccount)")
    public ResponseEntity<RestApiResponse<IdResponse>> profileUpdate(
            @CurrentAccount CurrentAccountDTO currentAccount,
            @Valid @RequestBody AccountProfileUpdateRequest accountProfileUpdateRequest
    ) {
        Long updatedId = accountCommandService.updateProfile(
                AccountProfileUpdateCommand.from(currentAccount, accountProfileUpdateRequest)
        );
        IdResponse updateResponse = IdResponse.of(updatedId);

        return restApiController.ok(updateResponse);
    }

    @Operation(summary = "내 계정 탈퇴(비활성화)")
    @DeleteMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated() and @memberGuard.canAccessSelf(#currentAccount)")
    public ResponseEntity<Void> withdraw(
            @CurrentAccount CurrentAccountDTO currentAccount,
            HttpServletRequest request
    ) {
        String accessToken = accessTokenResolver.resolveAccessToken(request).orElse(null);
        accountCommandService.withdraw(AccountWithdrawCommand.of(currentAccount, accessToken));
        return restApiController.noContent();
    }
}
