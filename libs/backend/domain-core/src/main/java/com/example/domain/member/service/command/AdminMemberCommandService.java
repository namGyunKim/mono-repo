package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.member.entity.Member;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.support.*;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminMemberCommandService extends AbstractMemberCommandService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberUniquenessSupport memberUniquenessSupport;

    private final MemberImageStoragePort memberImageStoragePort;
    private final MemberSocialCleanupPort memberSocialCleanupPort;

    private final MemberActivityPublishPort memberActivityPublishPort;
    private final MemberTokenRevocationPort memberTokenRevocationPort;
    private final MemberPermissionCheckPort memberPermissionCheckPort;

    @Override
    public List<AccountRole> getSupportedRoles() {
        return List.of(AccountRole.ADMIN, AccountRole.SUPER_ADMIN);
    }

    @Override
    public Long createMember(MemberCreateCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 생성 요청 값은 필수입니다.");
        }

        // [보안]
        // ADMIN/SUPER_ADMIN 생성 요청에서 role 파라미터가 변조되더라도,
        // 관리자 CommandService에서는 관리자 권한만 허용합니다.
        if (command.role() == null || (command.role() != AccountRole.ADMIN && command.role() != AccountRole.SUPER_ADMIN)) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "관리자 생성에서는 ADMIN 또는 SUPER_ADMIN 권한만 허용됩니다.");
        }

        // [중요]
        // GenerationType.IDENTITY 환경에서는 save 시점에 insert가 즉시 발생할 수 있어,
        // save 이후에 비밀번호를 암호화하면 "평문 insert -> 암호화 update"가 발생할 수 있습니다.
        // 따라서 저장 전에 반드시 비밀번호를 암호화하여 Member를 생성합니다.
        final String encodedPassword = passwordEncoder.encode(command.password());
        final Member member = memberRepository.save(Member.from(command, encodedPassword));

        final AccountRole role = member.getRole();
        final String details = (role == AccountRole.SUPER_ADMIN) ? "최고 관리자 계정 생성" : "관리자 계정 생성";
        memberActivityPublishPort.publishMemberActivity(member.getLoginId(), member.getId(), LogType.JOIN, details);

        return member.getId();
    }

    @Override
    public Long updateMember(MemberUpdateCommand command) {
        if (command == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 수정 요청 값은 필수입니다.");
        }

        final Member member = findAdminMember(command.memberId());
        updateMemberCommon(member, command, MemberUpdateContext.of(
                memberUniquenessSupport, passwordEncoder, memberActivityPublishPort,
                true, "관리자 비밀번호 변경", "관리자 정보 수정"
        ));

        return member.getId();
    }

    /**
     * 관리자/최고 관리자 회원 탈퇴 (비활성화) 로직
     * 1. 회원의 active 상태를 INACTIVE로 변경 및 개인정보 마스킹 (withdraw)
     * 2. S3에 저장된 프로필 이미지 삭제
     */
    @Override
    public Long deactivateMember(MemberDeactivateCommand command) {
        if (command == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "탈퇴 요청 값은 필수입니다.");
        }

        final Member member = findAdminMember(command.memberId());
        deactivateMemberCommon(member, MemberDeactivateContext.of(
                memberImageStoragePort, memberSocialCleanupPort, memberActivityPublishPort, "관리자 탈퇴/비활성화 처리"
        ));
        revokeSelfLogoutIfNeeded(command);

        return member.getId();
    }

    @Override
    public void updateMemberRole(MemberRoleUpdateCommand command) {
        validateRoleUpdateCommand(command);
        validateNotSelfRoleChange(command.memberId());

        final Member member = findAdminMember(command.memberId());
        applyRoleChange(member, command.role(), memberActivityPublishPort);
    }

    private void validateNotSelfRoleChange(final Long memberId) {
        if (memberPermissionCheckPort.isSameMember(memberId)) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "자신의 등급은 변경할 수 없습니다.");
        }
    }

    private Member findAdminMember(Long memberId) {
        if (memberId == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "memberId는 필수입니다.");
        }

        return memberRepository.findByIdAndRoleIn(
                        memberId,
                        List.of(AccountRole.ADMIN, AccountRole.SUPER_ADMIN)
                )
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));
    }

    private void revokeSelfLogoutIfNeeded(MemberDeactivateCommand command) {
        if (command == null) {
            return;
        }
        final Long memberId = command.memberId();
        final Long currentAccountId = command.currentAccountId();
        if (memberId == null || currentAccountId == null) {
            return;
        }
        if (!memberId.equals(currentAccountId)) {
            return;
        }
        if (command.logoutCommand() == null) {
            return;
        }
        memberTokenRevocationPort.revokeOnLogout(command.logoutCommand());
    }
}
