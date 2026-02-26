package com.example.domain.account.payload.dto;

import com.example.domain.account.payload.request.AccountProfileUpdateRequest;

/**
 * 계정 프로필 수정 Command DTO
 */
public record AccountProfileUpdateCommand(
        CurrentAccountDTO currentAccount,
        String nickName,
        String password
) {

    public static AccountProfileUpdateCommand from(CurrentAccountDTO currentAccount, AccountProfileUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return of(currentAccount, request.nickName(), request.password());
    }

    public static AccountProfileUpdateCommand of(CurrentAccountDTO currentAccount, String nickName, String password) {
        if (currentAccount == null) {
            throw new IllegalArgumentException("currentAccount는 필수입니다.");
        }
        if (nickName == null || nickName.isBlank()) {
            throw new IllegalArgumentException("nickName은 필수입니다.");
        }
        return new AccountProfileUpdateCommand(currentAccount, nickName, password);
    }
}
