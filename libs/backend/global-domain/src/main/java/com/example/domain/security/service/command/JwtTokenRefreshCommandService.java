package com.example.domain.security.service.command;

import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.domain.security.jwt.JwtTokenParseResult;
import com.example.domain.security.jwt.JwtTokenParser;
import com.example.domain.security.jwt.JwtTokenPayload;
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
@RequiredArgsConstructor
public class JwtTokenRefreshCommandService {

    private final JwtTokenCommandService jwtTokenCommandService;
    private final JwtTokenParser jwtTokenParser;
    private final BlacklistedTokenCommandService blacklistedTokenCommandService;
    private final BlacklistedTokenChecker blacklistedTokenChecker;
    private final MemberRepository memberRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;

    @Transactional
    public RefreshTokenResponse refreshTokens(RefreshTokenIssueCommand command) {
        String refreshToken = resolveRefreshToken(command);
        JwtTokenPayload payload = parseRefreshToken(refreshToken);
        Member member = loadMemberForRefresh(payload);
        validateTokenVersion(member, payload);
        validateNotBlacklisted(member, refreshToken);
        validateStoredRefreshToken(member, refreshToken);
        return reissueTokens(member, refreshToken);
    }

    private String resolveRefreshToken(RefreshTokenIssueCommand command) {
        if (command == null || !StringUtils.hasText(command.refreshToken())) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "refreshToken은 필수입니다.");
        }
        return command.refreshToken().trim();
    }

    private JwtTokenPayload parseRefreshToken(String refreshToken) {
        JwtTokenParseResult parseResult = jwtTokenParser.parseTokenResult(refreshToken);
        if (parseResult.status() == JwtTokenParseStatus.EXPIRED) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_EXPIRED, "리프레시 토큰이 만료되었습니다.");
        }
        if (parseResult.status() != JwtTokenParseStatus.VALID) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }

        JwtTokenPayload payload = parseResult.payload();
        if (payload == null || payload.tokenType() != JwtTokenType.REFRESH || !StringUtils.hasText(payload.subject())) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
        return payload;
    }

    private Member loadMemberForRefresh(JwtTokenPayload payload) {
        Member member = memberRepository.findByLoginId(payload.subject())
                .orElseThrow(() -> new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다."));

        if (member.getActive() != MemberActiveStatus.ACTIVE) {
            throw new GlobalException(ErrorCode.MEMBER_INACTIVE, "비활성화된 계정입니다.");
        }

        return member;
    }

    private void validateTokenVersion(Member member, JwtTokenPayload payload) {
        if (member.getTokenVersion() != payload.tokenVersion()) {
            throw new GlobalException(ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private void validateNotBlacklisted(Member member, String refreshToken) {
        if (blacklistedTokenChecker.isBlacklisted(refreshToken)) {
            throw revokeAndCreateException(member, refreshToken, ErrorCode.REFRESH_TOKEN_REVOKED, "이미 폐기된 리프레시 토큰입니다.");
        }
    }

    private void validateStoredRefreshToken(Member member, String refreshToken) {
        String storedRefreshTokenEncrypted = member.getRefreshTokenEncrypted();
        String storedRefreshToken;
        try {
            storedRefreshToken = refreshTokenCrypto.decrypt(storedRefreshTokenEncrypted);
        } catch (IllegalStateException e) {
            throw revokeAndCreateException(member, refreshToken, ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }

        if (!StringUtils.hasText(storedRefreshToken) || !storedRefreshToken.equals(refreshToken)) {
            throw revokeAndCreateException(member, refreshToken, ErrorCode.REFRESH_TOKEN_INVALID, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private RefreshTokenResponse reissueTokens(Member member, String refreshToken) {
        String accessToken = jwtTokenCommandService.generateAccessToken(member);
        String newRefreshToken = jwtTokenCommandService.generateRefreshToken(member);
        String newRefreshTokenEncrypted = refreshTokenCrypto.encrypt(newRefreshToken);

        blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(refreshToken));
        member.updateRefreshTokenEncrypted(newRefreshTokenEncrypted);

        return RefreshTokenResponse.of(accessToken, newRefreshToken);
    }

    private void revokeOnSuspiciousRefresh(Member member, String refreshToken) {
        if (member == null) {
            return;
        }
        blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(refreshToken));
        member.rotateTokenVersion();
        member.invalidateRefreshTokenEncrypted();
    }

    private GlobalException revokeAndCreateException(Member member, String refreshToken, ErrorCode errorCode, String message) {
        revokeOnSuspiciousRefresh(member, refreshToken);
        return new GlobalException(errorCode, message);
    }
}
