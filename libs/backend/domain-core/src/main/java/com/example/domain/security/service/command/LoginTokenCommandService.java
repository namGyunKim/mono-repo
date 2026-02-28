package com.example.domain.security.service.command;

import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.domain.security.support.SecurityMemberTokenPort;
import com.example.domain.security.support.payload.SecurityMemberTokenInfo;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.RefreshTokenCrypto;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.payload.LoginTokenIssueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginTokenCommandService {

    private final SecurityMemberTokenPort securityMemberTokenPort;
    private final JwtTokenCommandService jwtTokenCommandService;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final BlacklistedTokenCommandService blacklistedTokenCommandService;

    @Transactional
    public LoginTokenResponse issueTokens(LoginTokenIssueCommand command) {
        SecurityMemberTokenInfo memberInfo = findMemberTokenInfo(command);
        blacklistPreviousRefreshTokenIfPresent(memberInfo);
        String accessToken = jwtTokenCommandService.generateAccessToken(memberInfo);
        String refreshToken = jwtTokenCommandService.generateRefreshToken(memberInfo);
        String refreshTokenEncrypted = refreshTokenCrypto.encrypt(refreshToken);
        securityMemberTokenPort.updateRefreshTokenEncrypted(memberInfo.id(), refreshTokenEncrypted);

        LoginMemberView memberView = LoginMemberView.of(
                memberInfo.id(),
                memberInfo.loginId(),
                memberInfo.role(),
                memberInfo.nickName(),
                memberInfo.memberType(),
                memberInfo.active()
        );
        return LoginTokenResponse.from(memberView, accessToken, refreshToken);
    }

    private void blacklistPreviousRefreshTokenIfPresent(SecurityMemberTokenInfo memberInfo) {
        if (memberInfo == null) {
            return;
        }
        String storedRefreshTokenEncrypted = memberInfo.refreshTokenEncrypted();
        if (!StringUtils.hasText(storedRefreshTokenEncrypted)) {
            return;
        }

        try {
            String storedRefreshToken = refreshTokenCrypto.decrypt(storedRefreshTokenEncrypted);
            if (StringUtils.hasText(storedRefreshToken)) {
                blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(storedRefreshToken));
            }
        } catch (IllegalStateException e) {
            // 키 회전/손상 등으로 복호화가 실패하면 기존 토큰은 사실상 폐기된 것으로 간주합니다.
        }
    }

    private SecurityMemberTokenInfo findMemberTokenInfo(LoginTokenIssueCommand command) {
        if (command == null || command.memberId() == null || command.memberId() <= 0) {
            throw new GlobalException(ErrorCode.MEMBER_NOT_EXIST);
        }
        return securityMemberTokenPort.findTokenInfoById(command.memberId())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));
    }
}
