package com.example.domain.account.service.command;

import com.example.domain.account.payload.dto.AccountActivityPublishCommand;
import com.example.domain.account.payload.dto.AccountLogoutCommand;
import com.example.domain.account.payload.dto.AccountProfileUpdateCommand;
import com.example.domain.account.payload.dto.AccountWithdrawCommand;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.support.AccountMemberCommandPort;
import com.example.domain.account.support.AccountActivityPublishPort;
import com.example.domain.account.support.AccountTokenRevocationPort;
import com.example.domain.account.validator.AccountInputValidator;
import com.example.domain.log.enums.LogType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountCommandService {

    private final AccountMemberCommandPort accountMemberCommandPort;
    private final AccountActivityPublishPort accountActivityPublishPort;
    private final AccountTokenRevocationPort accountTokenRevocationPort;

    public void logout(AccountLogoutCommand command) {
        validateLogoutCommand(command);

        CurrentAccountDTO currentAccount = command.currentAccount();
        accountActivityPublishPort.publishMemberActivity(
                AccountActivityPublishCommand.of(currentAccount.loginId(), currentAccount.id(), LogType.LOGOUT, "로그아웃")
        );
        accountTokenRevocationPort.revokeOnLogout(currentAccount.id(), command.accessToken());
    }

    public void withdraw(AccountWithdrawCommand command) {
        validateWithdrawCommand(command);

        CurrentAccountDTO currentAccount = command.currentAccount();
        // 계정 탈퇴 응답 시점에 회원 비활성화 상태가 즉시 반영되어야 하므로 동일 트랜잭션 동기 호출을 유지합니다.
        accountMemberCommandPort.deactivateMember(currentAccount.role(), currentAccount.id());
        accountTokenRevocationPort.revokeOnLogout(currentAccount.id(), command.accessToken());
    }

    public Long updateProfile(AccountProfileUpdateCommand command) {
        validateProfileUpdateCommand(command);

        return accountMemberCommandPort.updateMemberProfile(command);
    }

    private void validateLogoutCommand(AccountLogoutCommand command) {
        AccountInputValidator.requireNonNull(command, "로그아웃 요청 값이 비어있습니다.");
        AccountInputValidator.requireCurrentAccountFull(command.currentAccount());
    }

    private void validateWithdrawCommand(AccountWithdrawCommand command) {
        AccountInputValidator.requireNonNull(command, "탈퇴 요청 값이 비어있습니다.");
        AccountInputValidator.requireCurrentAccountFull(command.currentAccount());
    }

    private void validateProfileUpdateCommand(AccountProfileUpdateCommand command) {
        AccountInputValidator.requireNonNull(command, "프로필 수정 요청 값이 비어있습니다.");
        AccountInputValidator.requireCurrentAccountFull(command.currentAccount());
        AccountInputValidator.requireHasText(command.nickName(), "nickName은 필수입니다.");
    }

}
