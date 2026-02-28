package com.example.domain.member.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.init.support.InitMemberSeedPort;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.service.MemberStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * InitMemberSeedPort 어댑터 — member 도메인이 init 도메인에 시드 기능을 제공한다.
 *
 * <p>
 * MemberStrategyFactory를 경유하여 역할별 Command/Query 서비스에 위임한다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class InitMemberSeedPortAdapter implements InitMemberSeedPort {

    private final MemberStrategyFactory memberStrategyFactory;

    @Override
    public boolean existsByRole(AccountRole role) {
        return memberStrategyFactory.getQueryService(role)
                .existsByRole(MemberRoleExistsQuery.of(role));
    }

    @Override
    public Long seedMember(String loginId, String nickName, String password, AccountRole role) {
        final MemberCreateCommand command = MemberCreateCommand.of(
                loginId,
                nickName,
                password,
                role,
                MemberType.GENERAL
        );
        return memberStrategyFactory.getCommandService(role).createMember(command);
    }
}
