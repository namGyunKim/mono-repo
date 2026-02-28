package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberSocialCleanupPort;
import com.example.domain.member.support.MemberUniquenessSupport;
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
public class UserMemberCommandService extends AbstractMemberCommandService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberUniquenessSupport memberUniquenessSupport;
    private final MemberImageStoragePort memberImageStoragePort;
    private final MemberSocialCleanupPort memberSocialCleanupPort;
    private final ActivityEventPublisher activityEventPublisher;

    @Override
    public List<AccountRole> getSupportedRoles() {
        return List.of(AccountRole.USER);
    }

    @Override
    public Long createMember(MemberCreateCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 생성 요청 값은 필수입니다.");
        }

        // [보안]
        // /member/user/create 요청에서 role 파라미터가 변조되더라도,
        // USER CommandService에서는 항상 USER 권한으로만 생성되도록 강제합니다.
        MemberCreateCommand safeCommand = command.withRole(AccountRole.USER);

        // [중요]
        // GenerationType.IDENTITY 환경에서는 save 시점에 insert가 즉시 발생할 수 있어,
        // save 이후에 비밀번호를 암호화하면 "평문 insert -> 암호화 update"가 발생할 수 있습니다.
        // 따라서 저장 전에 반드시 비밀번호를 암호화하여 Member를 생성합니다.
        String encodedPassword = passwordEncoder.encode(safeCommand.password());
        Member member = memberRepository.save(Member.from(safeCommand, encodedPassword));

        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(member.getLoginId(), member.getId(), LogType.JOIN, "일반 회원 가입")
        );
        return member.getId();
    }

    @Override
    public Long updateMember(MemberUpdateCommand command) {
        if (command == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 수정 요청 값은 필수입니다.");
        }

        Member member = findUserMember(command.memberId());
        boolean allowPasswordChange = member.getMemberType() == MemberType.GENERAL;
        updateMemberCommon(
                member,
                command,
                memberUniquenessSupport,
                passwordEncoder,
                activityEventPublisher,
                allowPasswordChange,
                "비밀번호 변경",
                "회원 정보 수정"
        );

        return member.getId();
    }

    @Override
    public Long deactivateMember(MemberDeactivateCommand command) {
        if (command == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "탈퇴 요청 값은 필수입니다.");
        }

        Member member = findUserMember(command.memberId());
        deactivateMemberCommon(
                member,
                memberImageStoragePort,
                memberSocialCleanupPort,
                activityEventPublisher,
                "회원 탈퇴 처리"
        );
        return member.getId();
    }

    @Override
    public void updateMemberRole(MemberRoleUpdateCommand command) {
        if (command == null || command.role() == null || command.memberId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "변경할 권한 값은 필수입니다.");
        }
        if (command.role() == AccountRole.GUEST) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "GUEST 권한은 설정할 수 없습니다.");
        }

        AccountRole newRole = command.role();
        Member member = findUserMember(command.memberId());
        AccountRole oldRole = member.getRole();
        member.changeRole(newRole);
        member.rotateTokenVersion();
        member.invalidateRefreshTokenEncrypted();
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        member.getLoginId(),
                        member.getId(),
                        LogType.UPDATE,
                        "권한 변경: %s -> %s".formatted(oldRole, newRole)
                )
        );
    }

    private Member findUserMember(Long memberId) {
        if (memberId == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "memberId는 필수입니다.");
        }

        Member member = memberRepository.findByIdAndRoleIn(
                        memberId,
                        List.of(AccountRole.USER)
                )
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        return member;
    }

}
