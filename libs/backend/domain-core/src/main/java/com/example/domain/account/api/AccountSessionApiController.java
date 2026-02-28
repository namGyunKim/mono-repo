package com.example.domain.account.api;

import com.example.domain.account.payload.dto.AccountLogoutCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.service.command.AccountCommandService;
import com.example.global.annotation.CurrentAccount;
import com.example.global.api.RestApiController;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.version.ApiVersioning;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SessionApiController", description = "세션 관련 REST API")
@ConditionalOnProperty(name = "app.type", havingValue = "user")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class AccountSessionApiController {

    private final AccountCommandService accountCommandService;
    private final RestApiController restApiController;
    private final AccessTokenResolver accessTokenResolver;

    @Operation(summary = "로그아웃")
    @DeleteMapping(value = "/me", version = ApiVersioning.V1)
    @PreAuthorize("@memberGuard.isAuthenticated()")
    public ResponseEntity<Void> logout(
            @CurrentAccount CurrentAccountDTO currentAccount,
            HttpServletRequest request
    ) {
        final String accessToken = accessTokenResolver.resolveAccessToken(request).orElse(null);
        accountCommandService.logout(AccountLogoutCommand.of(currentAccount, accessToken));
        return restApiController.noContent();
    }
}
