package com.example.domain.init.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.init.support.InitMemberSeedPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 초기 데이터 시드 커맨드 서비스
 *
 * <p>
 * 애플리케이션 기동 시 필수 시드 데이터를 생성한다.
 * member 도메인 직접 참조 없이 InitMemberSeedPort를 경유한다.
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@Profile("local")
public class InitCommandService {

    private final InitMemberSeedPort initMemberSeedPort;

    /**
     * 서버 시작 시 실행
     *
     * <p>
     * - @PostConstruct는 JPA 초기화/DDL 생성 타이밍에 따라 실행 시점 이슈가 발생할 수 있어,
     * ApplicationReadyEvent 이후로 시드를 실행한다.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initOnApplicationReady() {
        seedSuperAdmin();
        // 필요하면 아래 주석을 해제해서 샘플 계정을 추가 생성
        // seedAdmins();
        // seedUsers();
    }

    /**
     * 최고 관리자가 없을 경우 생성한다.
     */
    private void seedSuperAdmin() {
        if (initMemberSeedPort.existsByRole(AccountRole.SUPER_ADMIN)) {
            log.info("[Init] SUPER_ADMIN 계정이 이미 존재합니다. 시드 생략");
            return;
        }

        initMemberSeedPort.seedMember("superAdmin", "최고관리자", "1234", AccountRole.SUPER_ADMIN);
        log.info("[Init] SUPER_ADMIN 기본 계정 생성 완료. loginId=superAdmin, role=SUPER_ADMIN");
    }

    // 관리자가 없을 경우 관리자 10명 생성
    // private void seedAdmins() {
    //     if (initMemberSeedPort.existsByRole(AccountRole.ADMIN)) {
    //         return;
    //     }
    //     for (int i = 1; i <= 10; i++) {
    //         initMemberSeedPort.seedMember("admin" + i, "관리자" + i, "1234", AccountRole.ADMIN);
    //     }
    // }

    // 유저가 없을 경우 유저 51명 생성
    // private void seedUsers() {
    //     if (initMemberSeedPort.existsByRole(AccountRole.USER)) {
    //         return;
    //     }
    //     for (int i = 1; i <= 51; i++) {
    //         initMemberSeedPort.seedMember("user" + i, "유저이름" + i, "1234", AccountRole.USER);
    //     }
    // }
}
