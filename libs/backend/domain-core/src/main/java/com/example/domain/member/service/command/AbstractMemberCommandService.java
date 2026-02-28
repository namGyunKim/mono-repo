package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.member.entity.Member;
import com.example.domain.member.entity.MemberImage;
import com.example.domain.member.enums.MemberUploadDirect;
import com.example.domain.member.payload.dto.MemberImagesStorageDeleteCommand;
import com.example.domain.member.payload.dto.MemberNickNameExclusiveDuplicateCheckQuery;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.support.MemberActivityPublishPort;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberUniquenessSupport;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Transactional
public abstract class AbstractMemberCommandService implements MemberCommandService {

    // 해당 서비스가 처리할 수 있는 권한 목록 반환
    public abstract List<AccountRole> getSupportedRoles();

    protected void updateMemberCommon(Member member, MemberUpdateCommand command, MemberUpdateContext context) {
        validateUpdateMemberInput(member, command);

        final String loginId = member.getLoginId();
        validateNickNameUniqueness(command, loginId, context.memberUniquenessSupport());
        member.changeNickName(command.nickName());
        updatePasswordIfAllowed(member, command, context, loginId);
        publishMemberUpdatedEvent(context.memberActivityPublishPort(), member, context.updateMessage(), loginId);
    }

    protected void deactivateMemberCommon(Member member, MemberDeactivateContext context) {
        validateDeactivateMemberInput(member);

        final String loginId = member.getLoginId();
        member.withdraw();
        context.memberSocialCleanupPort().cleanupOnWithdraw(member.getId(), loginId);
        deleteMemberProfileImages(member, context.memberImageStoragePort());
        publishMemberDeactivatedEvent(context.memberActivityPublishPort(), member, context.inactiveMessage(), loginId);
    }

    private void validateUpdateMemberInput(Member member, MemberUpdateCommand command) {
        if (member == null || command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 수정 요청 값은 필수입니다.");
        }
    }

    private void validateNickNameUniqueness(
            MemberUpdateCommand command,
            String loginId,
            MemberUniquenessSupport memberUniquenessSupport
    ) {
        if (memberUniquenessSupport.isNickNameDuplicatedExceptLoginId(
                MemberNickNameExclusiveDuplicateCheckQuery.of(command.nickName(), loginId))) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "이미 등록된 닉네임입니다.");
        }
    }

    private void updatePasswordIfAllowed(
            Member member,
            MemberUpdateCommand command,
            MemberUpdateContext context,
            String loginId
    ) {
        if (!context.allowPasswordChange() || !StringUtils.hasText(command.password())) {
            return;
        }

        member.updatePassword(context.passwordEncoder().encode(command.password()));
        member.rotateTokenVersion();
        member.invalidateRefreshTokenEncrypted();

        context.memberActivityPublishPort().publishMemberActivity(
                loginId,
                member.getId(),
                LogType.PASSWORD_CHANGE,
                context.passwordChangeMessage()
        );
    }

    private void publishMemberUpdatedEvent(
            MemberActivityPublishPort memberActivityPublishPort,
            Member member,
            String updateMessage,
            String loginId
    ) {
        memberActivityPublishPort.publishMemberActivity(
                loginId,
                member.getId(),
                LogType.UPDATE,
                updateMessage
        );
    }

    private void validateDeactivateMemberInput(Member member) {
        if (member == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "탈퇴 요청 값은 필수입니다.");
        }
    }

    private void deleteMemberProfileImages(Member member, MemberImageStoragePort memberImageStoragePort) {
        if (member.getMemberImages().isEmpty()) {
            return;
        }

        final List<String> fileNames = member.getMemberImages().stream()
                .filter(mi -> mi.getUploadDirect() == MemberUploadDirect.MEMBER_PROFILE)
                .map(MemberImage::getFileName)
                .toList();

        if (!fileNames.isEmpty()) {
            memberImageStoragePort.deleteImages(
                    MemberImagesStorageDeleteCommand.of(
                            member.getId(),
                            fileNames,
                            MemberUploadDirect.MEMBER_PROFILE
                    )
            );
        }
        member.getMemberImages().clear();
    }

    private void publishMemberDeactivatedEvent(
            MemberActivityPublishPort memberActivityPublishPort,
            Member member,
            String inactiveMessage,
            String loginId
    ) {
        memberActivityPublishPort.publishMemberActivity(
                loginId,
                member.getId(),
                LogType.INACTIVE,
                inactiveMessage
        );
    }

    // ========== Role Update Common Logic ==========

    /**
     * 권한 변경 요청의 기본 검증을 수행합니다.
     *
     * @param command 권한 변경 요청
     * @throws GlobalException 검증 실패 시
     */
    protected void validateRoleUpdateCommand(
            final com.example.domain.member.payload.dto.MemberRoleUpdateCommand command
    ) {
        if (command == null || command.role() == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "변경할 권한 값은 필수입니다.");
        }
        if (command.role() == AccountRole.GUEST) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "GUEST 권한은 설정할 수 없습니다.");
        }
    }

    /**
     * 회원의 권한을 변경하고 관련 보안 토큰을 무효화합니다.
     *
     * @param member                    대상 회원
     * @param newRole                   새 권한
     * @param memberActivityPublishPort 활동 로그 발행 포트
     */
    protected void applyRoleChange(
            final Member member,
            final AccountRole newRole,
            final MemberActivityPublishPort memberActivityPublishPort
    ) {
        final AccountRole oldRole = member.getRole();
        member.changeRole(newRole);
        member.rotateTokenVersion();
        member.invalidateRefreshTokenEncrypted();

        memberActivityPublishPort.publishMemberActivity(
                member.getLoginId(),
                member.getId(),
                LogType.UPDATE,
                "권한 변경: %s -> %s".formatted(oldRole, newRole)
        );
    }
}
