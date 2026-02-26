package com.example.domain.account.service.command;

import com.example.domain.account.payload.dto.AccountLogoutCommand;
import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.payload.dto.AccountWithdrawCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberActivityCommand;
import com.example.domain.log.service.command.ActivityEventPublisher;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;
import com.example.domain.member.service.MemberStrategyFactory;
import com.example.domain.member.service.command.MemberCommandService;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.payload.SecurityLogoutCommand;
import com.example.global.security.service.command.JwtTokenRevocationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountCommandService {

    private final MemberStrategyFactory memberStrategyFactory;
    private final ActivityEventPublisher activityEventPublisher;
    private final JwtTokenRevocationCommandService jwtTokenRevocationCommandService;

    public void logout(AccountLogoutCommand command) {
        validateLogoutCommand(command);

        CurrentAccountDTO currentAccount = command.currentAccount();
        activityEventPublisher.publishMemberActivity(
                MemberActivityCommand.of(
                        currentAccount.loginId(),
                        currentAccount.id(),
                        LogType.LOGOUT,
                        "로그아웃"
                )
        );
        jwtTokenRevocationCommandService.revokeOnLogout(
                SecurityLogoutCommand.of(currentAccount.id(), command.accessToken())
        );
    }

    public void withdraw(AccountWithdrawCommand command) {
        validateWithdrawCommand(command);

        CurrentAccountDTO currentAccount = command.currentAccount();
        // 계정 탈퇴 응답 시점에 회원 비활성화 상태가 즉시 반영되어야 하므로 동일 트랜잭션 동기 호출을 유지합니다.
        MemberCommandService commandMemberService = memberStrategyFactory.getCommandService(currentAccount.role());
        commandMemberService.deactivateMember(
                MemberDeactivateCommand.of(currentAccount.id())
        );
        jwtTokenRevocationCommandService.revokeOnLogout(
                SecurityLogoutCommand.of(currentAccount.id(), command.accessToken())
        );
    }

    public Long updateProfile(AccountProfileUpdateCommand command) {
        validateProfileUpdateCommand(command);

        CurrentAccountDTO currentAccount = command.currentAccount();
        // 프로필 변경 성공 응답 직후 조회 일관성을 보장하기 위해 동일 트랜잭션 내에서 회원 업데이트를 수행합니다.
        MemberCommandService commandMemberService = memberStrategyFactory.getCommandService(currentAccount.role());
        MemberUpdateCommand updateCommand = MemberUpdateCommand.of(
                command.nickName(),
                command.password(),
                currentAccount.id()
        );
        return commandMemberService.updateMember(updateCommand);
    }

    private void validateLogoutCommand(AccountLogoutCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "로그아웃 요청 값이 비어있습니다.");
        }
        validateCurrentAccount(command.currentAccount());
    }

    private void validateWithdrawCommand(AccountWithdrawCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "탈퇴 요청 값이 비어있습니다.");
        }
        validateCurrentAccount(command.currentAccount());
    }

    private void validateProfileUpdateCommand(AccountProfileUpdateCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "프로필 수정 요청 값이 비어있습니다.");
        }
        validateCurrentAccount(command.currentAccount());
        if (command.nickName() == null || command.nickName().isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "nickName은 필수입니다.");
        }
    }

    private void validateCurrentAccount(CurrentAccountDTO currentAccount) {
        if (currentAccount == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 정보가 비어있습니다.");
        }
        if (currentAccount.id() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 식별자는 필수입니다.");
        }
        if (currentAccount.loginId() == null || currentAccount.loginId().isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 loginId는 필수입니다.");
        }
        if (currentAccount.role() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 role은 필수입니다.");
        }
        if (currentAccount.memberType() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 memberType은 필수입니다.");
        }
        if (currentAccount.nickName() == null || currentAccount.nickName().isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "현재 사용자 nickName은 필수입니다.");
        }
    }

}
