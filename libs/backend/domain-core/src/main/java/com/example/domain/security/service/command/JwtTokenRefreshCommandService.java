package com.example.domain.security.service.command;

import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.domain.security.jwt.JwtTokenParseResult;
import com.example.domain.security.jwt.JwtTokenParser;
import com.example.domain.security.jwt.JwtTokenPayload;
import com.example.domain.security.support.SecurityMemberTokenPort;
import com.example.domain.security.support.payload.SecurityMemberTokenInfo;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.RefreshTokenCrypto;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.blacklist.support.BlacklistedTokenChecker;
import com.example.global.security.jwt.JwtTokenParseStatus;
import com.example.global.security.jwt.JwtTokenType;
import com.example.global.security.payload.RefreshTokenIssueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenRefreshCommandService {

    private final JwtTokenCommandService jwtTokenCommandService;
    private final JwtTokenParser jwtTokenParser;
    private final BlacklistedTokenCommandService blacklistedTokenCommandService;
    private final BlacklistedTokenChecker blacklistedTokenChecker;
    private final SecurityMemberTokenPort securityMemberTokenPort;
    private final RefreshTokenCrypto refreshTokenCrypto;

    public RefreshTokenResponse refreshTokens(RefreshTokenIssueCommand command) {
        final String refreshToken = resolveRefreshToken(command);
        final JwtTokenPayload payload = parseRefreshToken(refreshToken);
        final SecurityMemberTokenInfo memberInfo = loadMemberInfoForRefresh(payload);
        validateTokenVersion(memberInfo, payload);
        validateNotBlacklisted(memberInfo, refreshToken);
        validateStoredRefreshToken(memberInfo, refreshToken);
        return reissueTokens(memberInfo, refreshToken);
    }

    private String resolveRefreshToken(RefreshTokenIssueCommand command) {
        if (command == null || !StringUtils.hasText(command.refreshToken())) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "refreshToken은 필수입니다.");
        }
        return command.refreshToken().trim();
    }

    private JwtTokenPayload parseRefreshToken(String refreshToken) {
        final JwtTokenParseResult parseResult = jwtTokenParser.parseTokenResult(refreshToken);
        if (parseResult.status() == JwtTokenParseStatus.EXPIRED) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_EXPIRED, "리프레시 토큰이 만료되었습니다.");
        }
        if (parseResult.status() != JwtTokenParseStatus.VALID) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }

        final JwtTokenPayload payload = parseResult.payload();
        if (payload == null || payload.tokenType() != JwtTokenType.REFRESH || !StringUtils.hasText(payload.subject())) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
        return payload;
    }

    private SecurityMemberTokenInfo loadMemberInfoForRefresh(JwtTokenPayload payload) {
        final SecurityMemberTokenInfo memberInfo = securityMemberTokenPort.findTokenInfoByLoginId(payload.subject())
                .orElseThrow(() -> new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다."));

        if (memberInfo.active() != MemberActiveStatus.ACTIVE) {
            throw new GlobalException(ErrorCode.MEMBER_INACTIVE, "비활성화된 계정입니다.");
        }

        return memberInfo;
    }

    private void validateTokenVersion(SecurityMemberTokenInfo memberInfo, JwtTokenPayload payload) {
        if (memberInfo.tokenVersion() != payload.tokenVersion()) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private void validateNotBlacklisted(SecurityMemberTokenInfo memberInfo, String refreshToken) {
        if (blacklistedTokenChecker.isBlacklisted(refreshToken)) {
            throw revokeAndCreateException(memberInfo, refreshToken, ErrorCode.REFRESH_TOKEN_REVOKED, "이미 폐기된 리프레시 토큰입니다.");
        }
    }

    private void validateStoredRefreshToken(SecurityMemberTokenInfo memberInfo, String refreshToken) {
        final String storedRefreshTokenEncrypted = memberInfo.refreshTokenEncrypted();
        final String storedRefreshToken;
        try {
            storedRefreshToken = refreshTokenCrypto.decrypt(storedRefreshTokenEncrypted);
        } catch (IllegalStateException e) {
            throw revokeAndCreateException(memberInfo, refreshToken, ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }

        if (!StringUtils.hasText(storedRefreshToken) || !storedRefreshToken.equals(refreshToken)) {
            throw revokeAndCreateException(memberInfo, refreshToken, ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private RefreshTokenResponse reissueTokens(SecurityMemberTokenInfo memberInfo, String refreshToken) {
        final String accessToken = jwtTokenCommandService.generateAccessToken(memberInfo);
        final String newRefreshToken = jwtTokenCommandService.generateRefreshToken(memberInfo);
        final String newRefreshTokenEncrypted = refreshTokenCrypto.encrypt(newRefreshToken);

        blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(refreshToken));
        securityMemberTokenPort.updateRefreshTokenEncrypted(memberInfo.id(), newRefreshTokenEncrypted);

        return RefreshTokenResponse.of(accessToken, newRefreshToken);
    }

    private void revokeOnSuspiciousRefresh(SecurityMemberTokenInfo memberInfo, String refreshToken) {
        if (memberInfo == null) {
            return;
        }
        blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(refreshToken));
        securityMemberTokenPort.revokeTokens(memberInfo.id());
    }

    private GlobalException revokeAndCreateException(SecurityMemberTokenInfo memberInfo, String refreshToken, ErrorCode errorCode, String message) {
        revokeOnSuspiciousRefresh(memberInfo, refreshToken);
        return new GlobalException(errorCode, message);
    }
}
