package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.entity.Member;
import com.example.domain.member.entity.MemberImage;
import com.example.domain.member.enums.MemberUploadDirect;
import com.example.domain.member.payload.dto.MemberImagesStorageDeleteCommand;
import com.example.domain.member.payload.dto.MemberNickNameDuplicateCheckCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberUniquenessSupport;
import com.example.domain.social.repository.SocialAccountRepository;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Transactional
public abstract class AbstractMemberCommandService implements MemberCommandService {

    // 해당 서비스가 처리할 수 있는 권한 목록 반환
    public abstract List<AccountRole> getSupportedRoles();

    protected void updateMemberCommon(
            Member member,
            MemberUpdateCommand command,
            MemberUniquenessSupport memberUniquenessSupport,
            PasswordEncoder passwordEncoder,
            ActivityEventPublisher activityEventPublisher,
            boolean allowPasswordChange,
            String passwordChangeMessage,
            String updateMessage
    ) {
        validateUpdateMemberInput(member, command);

        String loginId = member.getLoginId();
        validateNickNameUniqueness(command, loginId, memberUniquenessSupport);
        member.changeNickName(command.nickName());
        updatePasswordIfAllowed(
                member,
                command,
                allowPasswordChange,
                passwordEncoder,
                activityEventPublisher,
                passwordChangeMessage,
                loginId
        );
        publishMemberUpdatedEvent(activityEventPublisher, member, updateMessage, loginId);
    }

    protected void deactivateMemberCommon(
            Member member,
            MemberImageStoragePort memberImageStoragePort,
            SocialAccountRepository socialAccountRepository,
            ActivityEventPublisher activityEventPublisher,
            String inactiveMessage
    ) {
        validateDeactivateMemberInput(member);

        String loginId = member.getLoginId();
        member.withdraw();
        deleteSocialAccounts(member, socialAccountRepository);
        deleteMemberProfileImages(member, memberImageStoragePort);
        publishMemberDeactivatedEvent(activityEventPublisher, member, inactiveMessage, loginId);
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
                MemberNickNameDuplicateCheckCommand.of(command.nickName(), loginId))) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "이미 등록된 닉네임입니다.");
        }
    }

    private void updatePasswordIfAllowed(
            Member member,
            MemberUpdateCommand command,
            boolean allowPasswordChange,
            PasswordEncoder passwordEncoder,
            ActivityEventPublisher activityEventPublisher,
            String passwordChangeMessage,
            String loginId
    ) {
        if (!allowPasswordChange || !StringUtils.hasText(command.password())) {
            return;
        }

        member.updatePassword(passwordEncoder.encode(command.password()));
        member.rotateTokenVersion();
        member.invalidateRefreshTokenEncrypted();

        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        loginId,
                        member.getId(),
                        LogType.PASSWORD_CHANGE,
                        passwordChangeMessage
                )
        );
    }

    private void publishMemberUpdatedEvent(
            ActivityEventPublisher activityEventPublisher,
            Member member,
            String updateMessage,
            String loginId
    ) {
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        loginId,
                        member.getId(),
                        LogType.UPDATE,
                        updateMessage
                )
        );
    }

    private void validateDeactivateMemberInput(Member member) {
        if (member == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "탈퇴 요청 값은 필수입니다.");
        }
    }

    private void deleteSocialAccounts(Member member, SocialAccountRepository socialAccountRepository) {
        if (member.getId() > 0 && socialAccountRepository != null) {
            socialAccountRepository.deleteByMemberId(member.getId());
        }
    }

    private void deleteMemberProfileImages(Member member, MemberImageStoragePort memberImageStoragePort) {
        if (member.getMemberImages().isEmpty()) {
            return;
        }

        List<String> fileNames = member.getMemberImages().stream()
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
            ActivityEventPublisher activityEventPublisher,
            Member member,
            String inactiveMessage,
            String loginId
    ) {
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(loginId, member.getId(), LogType.INACTIVE, inactiveMessage)
        );
    }
}
