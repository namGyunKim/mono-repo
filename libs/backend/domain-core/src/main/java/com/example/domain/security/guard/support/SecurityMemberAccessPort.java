package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;

import java.util.List;
import java.util.Optional;

/**
 * Security 도메인이 회원 접근/인증 관련 정보를 조회할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인은 MemberRepository / Member 엔티티를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/guard/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * </p>
 */
public interface SecurityMemberAccessPort {

    /**
     * 회원 ID로 접근 대상 정보를 조회합니다.
     */
    Optional<MemberAccessTarget> findAccessTargetById(Long memberId);

    /**
     * 회원 ID와 역할 목록으로 접근 대상 정보를 조회합니다.
     */
    Optional<MemberAccessTarget> findAccessTargetByIdAndRoleIn(Long memberId, List<AccountRole> roles);

    /**
     * 로그인 ID로 활성 회원의 인증 정보를 조회합니다.
     * JWT 인증 필터에서 토큰 subject 기반 회원 조회에 사용합니다.
     */
    Optional<AccountAuthMemberView> findActiveAuthMemberByLoginId(String loginId);

    /**
     * 로그인 ID로 회원 ID를 조회합니다.
     * 로그인 실패 이벤트 발행 시 회원 식별에 사용합니다.
     */
    Optional<Long> findMemberIdByLoginId(String loginId);
}
