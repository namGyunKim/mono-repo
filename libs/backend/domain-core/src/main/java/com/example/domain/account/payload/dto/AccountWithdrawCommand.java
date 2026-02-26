package com.example.domain.account.payload.dto;

/**
 * 계정 탈퇴(비활성화) Command DTO
 */
public record AccountWithdrawCommand(
        CurrentAccountDTO currentAccount,
        String accessToken
) {

    public static AccountWithdrawCommand of(CurrentAccountDTO currentAccount, String accessToken) {
        return new AccountWithdrawCommand(currentAccount, accessToken);
    }
}
