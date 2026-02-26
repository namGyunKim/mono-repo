package com.example.domain.init.service;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.service.MemberStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Profile("local")
public class InitService {

    // MemberStrategyFactory를 주입받아 역할별 서비스를 동적으로 가져옵니다.
    private final MemberStrategyFactory memberStrategyFactory;

    /**
     * 서버 시작 시 실행
     *
     * <p>
     * - @PostConstruct는 JPA 초기화/DDL 생성 타이밍에 따라 실행 시점 이슈가 발생할 수 있어,
     * ApplicationReadyEvent 이후로 시드를 실행합니다.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initOnApplicationReady() {
        createMemberByRoleSuperAdmin();
        // 필요하면 아래 주석을 해제해서 샘플 계정을 추가 생성하세요.
        // createMemberByRoleAdmin();
        // createMemberByRoleUser();
    }

    // 최고 관리자가 없을경우 생성
    public void createMemberByRoleSuperAdmin() {
        if (!memberStrategyFactory.getQueryService(AccountRole.SUPER_ADMIN)
                .existsByRole(MemberRoleExistsQuery.of(AccountRole.SUPER_ADMIN))) {

            MemberCreateCommand command = MemberCreateCommand.of(
                    "superAdmin",
                    "최고관리자",
                    "1234",
                    AccountRole.SUPER_ADMIN,
                    MemberType.GENERAL
            );
            memberStrategyFactory.getCommandService(AccountRole.SUPER_ADMIN).createMember(command);

            log.info("[Init] SUPER_ADMIN 기본 계정 생성 완료. loginId=superAdmin, role=SUPER_ADMIN");
            return;
        }

        log.info("[Init] SUPER_ADMIN 계정이 이미 존재합니다. 시드 생략");
    }

    // 관리자가 없을경우 관리자 10개 생성
//    @Transactional
//    public void createMemberByRoleAdmin() {
//        if (!memberStrategyFactory.getQueryService(AccountRole.ADMIN)
//                .existsByRole(MemberRoleExistsQuery.of(AccountRole.ADMIN))) {
//            for (int i = 1; i <= 10; i++) {
//                MemberCreateCommand command = MemberCreateCommand.of("admin" + i, "관리자" + i, "1234", AccountRole.ADMIN, MemberType.GENERAL);
//                memberStrategyFactory.getCommandService(AccountRole.ADMIN).createMember(command);
//            }
//        }
//    }

    // 유저가 없을경우 유저 51개 생성
    @Transactional
    public void createMemberByRoleUser() {
        if (!memberStrategyFactory.getQueryService(AccountRole.USER)
                .existsByRole(MemberRoleExistsQuery.of(AccountRole.USER))) {
            for (int i = 1; i <= 51; i++) {
                MemberCreateCommand command = MemberCreateCommand.of("user" + i, "유저이름" + i, "1234", AccountRole.USER, MemberType.GENERAL);
                memberStrategyFactory.getCommandService(AccountRole.USER).createMember(command);
            }
        }
    }
}
