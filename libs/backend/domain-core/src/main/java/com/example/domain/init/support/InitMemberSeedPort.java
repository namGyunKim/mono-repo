package com.example.domain.init.support;

import com.example.domain.account.enums.AccountRole;

/**
 * 초기 데이터 시드를 위한 회원 생성 포트
 *
 * <p>
 * init 도메인이 member 도메인의 서비스/DTO를 직접 참조하지 않도록
 * Bounded Context 경계를 추상화한다.
 * </p>
 */
public interface InitMemberSeedPort {

    /**
     * 해당 역할의 회원이 존재하는지 확인한다.
     */
    boolean existsByRole(AccountRole role);

    /**
     * 초기 시드 회원을 생성하고 생성된 회원 ID를 반환한다.
     */
    Long seedMember(String loginId, String nickName, String password, AccountRole role);
}
