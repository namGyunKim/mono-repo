package com.example.domain.security.guard;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.security.guard.support.CurrentAccountProvider;
import com.example.domain.security.guard.support.MemberAccessTargetResolver;
import com.example.domain.security.guard.support.MemberStatusChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 회원(계정) 관리 권한을 중앙에서 판단하기 위한 Guard 클래스
 * <p>
 * [목적]
 * - Controller/Validator 등 여러 계층에 흩어지기 쉬운 권한 체크 로직을 단일 지점으로 모읍니다.
 * - 베이스 프로젝트에서 권한 정책을 확장/변경할 때 영향 범위를 최소화합니다.
 */
@Component
@RequiredArgsConstructor
public class MemberGuard {

    private final CurrentAccountProvider currentAccountProvider;
    private final MemberAccessTargetResolver memberAccessTargetResolver;
    private final MemberStatusChecker memberStatusChecker;

    /**
     * 관리자(ADMIN/SUPER_ADMIN) 계정 "목록 조회/생성" 등의 관리 기능은 SUPER_ADMIN만 허용합니다.
     * - USER 대상 관리: ADMIN/SUPER_ADMIN 허용 (Controller에서 @PreAuthorize로 1차 방어)
     * - ADMIN/SUPER_ADMIN 대상 관리: SUPER_ADMIN만 허용
     */
    public boolean canManageRole(AccountRole targetRole) {
        if (targetRole == null) {
            return false;
        }
        if (targetRole == AccountRole.ADMIN || targetRole == AccountRole.SUPER_ADMIN) {
            return isSuperAdmin();
        }
        return hasAnyRole(AccountRole.ADMIN, AccountRole.SUPER_ADMIN);
    }

    public boolean hasAnyAdminRole() {
        return hasAnyRole(AccountRole.ADMIN, AccountRole.SUPER_ADMIN);
    }

    public boolean isSuperAdmin() {
        return hasAnyRole(AccountRole.SUPER_ADMIN);
    }

    public boolean isAuthenticated() {
        return currentAccountProvider.isAuthenticated();
    }

    public Optional<CurrentAccountDTO> getCurrentAccount() {
        return currentAccountProvider.getCurrentAccount();
    }

    public CurrentAccountDTO getCurrentAccountOrGuest() {
        return currentAccountProvider.getCurrentAccountOrGuest();
    }

    public String getLoginIdOrDefault(String defaultLoginId) {
        return currentAccountProvider.getLoginIdOrDefault(defaultLoginId);
    }

    public boolean isSameMember(Long targetMemberId) {
        if (targetMemberId == null) {
            return false;
        }

        return currentAccountProvider.getCurrentMemberId()
                .map(currentMemberId -> currentMemberId.equals(targetMemberId))
                .orElse(false);
    }

    /**
     * 대상 회원(계정)에 대해 "상세/수정/비활성화" 등의 작업이 가능한지 확인합니다.
     * <p>
     * 정책:
     * - 본인: 허용
     * - SUPER_ADMIN: 모두 허용
     * - ADMIN: USER만 허용
     */
    public boolean canAccessMember(AccountRole targetRole, Long targetId) {
        return canAccessMemberInternal(targetRole, targetId, null);
    }

    /**
     * 대상 회원 ID 기준으로 "상세/수정/비활성화" 등의 작업 가능 여부를 확인합니다.
     * <p>
     * - 대상 Role은 저장된 엔티티로부터 확인합니다.
     * - 대상 회원이 존재하지 않으면 false를 반환합니다.
     */
    public boolean canAccessMember(Long targetId) {
        if (targetId == null) {
            return false;
        }

        return memberAccessTargetResolver.resolve(targetId)
                .map(target -> canAccessMemberInternal(target.role(), target.id(), target.active()))
                .orElse(false);
    }

    /**
     * 본인 계정 접근 가능 여부를 판단합니다.
     * <p>
     * - ADMIN/SUPER_ADMIN은 비활성화 상태여도 접근 허용(기존 정책 유지)
     * - USER는 ACTIVE 상태만 허용
     * </p>
     */
    public boolean canAccessSelf(CurrentAccountDTO currentAccount) {
        if (currentAccount == null || currentAccount.id() == null || currentAccount.role() == null) {
            return false;
        }

        if (currentAccount.role() == AccountRole.ADMIN || currentAccount.role() == AccountRole.SUPER_ADMIN) {
            return true;
        }

        return memberStatusChecker.isActiveMember(currentAccount.id(), currentAccount.role());
    }

    private boolean canAccessMemberInternal(AccountRole targetRole, Long targetId, MemberActiveStatus targetActive) {
        if (!isValidAccessTarget(targetRole, targetId)) {
            return false;
        }

        return getCurrentAccount()
                .map(currentAccount -> canCurrentAccountAccessTarget(
                        currentAccount,
                        targetRole,
                        targetId,
                        targetActive
                ))
                .orElse(false);
    }

    private boolean isValidAccessTarget(AccountRole targetRole, Long targetId) {
        return targetRole != null && targetId != null;
    }

    private boolean canCurrentAccountAccessTarget(
            CurrentAccountDTO currentAccount,
            AccountRole targetRole,
            Long targetId,
            MemberActiveStatus targetActive
    ) {
        AccountRole currentRole = currentAccount.role();
        if (currentRole == null) {
            return false;
        }
        if (!isRoleAccessAllowed(currentAccount, currentRole, targetRole, targetId)) {
            return false;
        }
        return isTargetStateAllowed(currentRole, targetId, targetRole, targetActive);
    }

    private boolean isRoleAccessAllowed(
            CurrentAccountDTO currentAccount,
            AccountRole currentRole,
            AccountRole targetRole,
            Long targetId
    ) {
        if (isSelf(currentAccount, targetId)) {
            return true;
        }
        if (currentRole == AccountRole.SUPER_ADMIN) {
            return true;
        }
        return currentRole == AccountRole.ADMIN && targetRole == AccountRole.USER;
    }

    private boolean isTargetStateAllowed(
            AccountRole currentRole,
            Long targetId,
            AccountRole targetRole,
            MemberActiveStatus targetActive
    ) {
        if (isAdminRole(currentRole)) {
            return true;
        }
        if (targetActive != null) {
            return targetActive == MemberActiveStatus.ACTIVE;
        }
        return memberStatusChecker.isActiveMember(targetId, targetRole);
    }

    private boolean isSelf(CurrentAccountDTO currentAccount, Long targetId) {
        return currentAccount.id() != null && currentAccount.id().equals(targetId);
    }

    private boolean isAdminRole(AccountRole role) {
        return role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN;
    }

    private boolean hasAnyRole(AccountRole... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }

        Optional<AccountRole> currentRole = currentAccountProvider.getCurrentRole();
        if (currentRole.isEmpty()) {
            return false;
        }

        AccountRole current = currentRole.get();
        return java.util.Arrays.stream(roles).anyMatch(role -> current == role);
    }
}
