package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.security.PrincipalDetails;
import com.example.global.security.SecurityContextManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CurrentAccountProvider {

    private final SecurityContextManager securityContextManager;

    public boolean isAuthenticated() {
        Authentication authentication = securityContextManager.getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public Optional<CurrentAccountDTO> getCurrentAccount() {
        return resolvePrincipal()
                .map(principal -> CurrentAccountDTO.of(
                        principal.getId(),
                        principal.getUsername(),
                        principal.getNickName(),
                        principal.getRole(),
                        principal.getMemberType()
                ));
    }

    public CurrentAccountDTO getCurrentAccountOrGuest() {
        return getCurrentAccount().orElse(CurrentAccountDTO.ofGuest());
    }

    public String getLoginIdOrDefault(String defaultLoginId) {
        return getCurrentLoginId()
                .or(this::getAuthenticationName)
                .orElse(defaultLoginId);
    }

    public Optional<AccountRole> getCurrentRole() {
        return getCurrentAccount().map(CurrentAccountDTO::role);
    }

    public Optional<Long> getCurrentMemberId() {
        return getCurrentAccount().map(CurrentAccountDTO::id);
    }

    public Optional<String> getCurrentLoginId() {
        return getCurrentAccount().map(CurrentAccountDTO::loginId);
    }

    public Optional<String> getAuthenticationName() {
        if (!isAuthenticated()) {
            return Optional.empty();
        }

        Authentication authentication = securityContextManager.getAuthentication();
        return Optional.ofNullable(authentication != null ? authentication.getName() : null);
    }

    private Optional<PrincipalDetails> resolvePrincipal() {
        Authentication authentication = securityContextManager.getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof PrincipalDetails principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }
}
