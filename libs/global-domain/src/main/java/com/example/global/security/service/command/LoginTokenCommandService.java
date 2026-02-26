package com.example.global.security.service.command;

import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.MemberRepository;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.security.RefreshTokenCrypto;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.global.security.payload.LoginTokenIssueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginTokenCommandService {

    private final MemberRepository memberRepository;
    private final JwtTokenCommandService jwtTokenCommandService;
    private final RefreshTokenCrypto refreshTokenCrypto;
    private final BlacklistedTokenCommandService blacklistedTokenCommandService;

    @Transactional
    public LoginTokenResponse issueTokens(LoginTokenIssueCommand command) {
        Member managedMember = findManagedMember(command);
        blacklistPreviousRefreshTokenIfPresent(managedMember);
        String accessToken = jwtTokenCommandService.generateAccessToken(managedMember);
        String refreshToken = jwtTokenCommandService.generateRefreshToken(managedMember);
        String refreshTokenEncrypted = refreshTokenCrypto.encrypt(refreshToken);
        managedMember.updateRefreshTokenEncrypted(refreshTokenEncrypted);

        LoginMemberView memberView = LoginMemberView.of(
                managedMember.getId(),
                managedMember.getLoginId(),
                managedMember.getRole(),
                managedMember.getNickName(),
                managedMember.getMemberType(),
                managedMember.getActive()
        );
        return LoginTokenResponse.from(memberView, accessToken, refreshToken);
    }

    private void blacklistPreviousRefreshTokenIfPresent(Member managedMember) {
        if (managedMember == null) {
            return;
        }
        String storedRefreshTokenEncrypted = managedMember.getRefreshTokenEncrypted();
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

    private Member findManagedMember(LoginTokenIssueCommand command) {
        if (command == null || command.memberId() == null || command.memberId() <= 0) {
            throw new GlobalException(ErrorCode.MEMBER_NOT_EXIST);
        }
        return memberRepository.findById(command.memberId())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));
    }
}
