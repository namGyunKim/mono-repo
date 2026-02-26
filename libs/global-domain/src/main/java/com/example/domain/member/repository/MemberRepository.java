package com.example.domain.member.repository;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, QuerydslPredicateExecutor<Member> {

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByLoginIdAndRole(String loginId, AccountRole role);

    Optional<LoginMemberViewProjection> findProjectedByLoginIdAndRole(String loginId, AccountRole role);

    boolean existsByLoginId(String loginId);

    boolean existsByNickName(String nickName);

    boolean existsByNickNameAndLoginIdNot(String nickName, String excludedLoginId);

    boolean existsByRole(AccountRole role);

    Optional<Member> findByIdAndRoleIn(Long id, List<AccountRole> roles);

    Optional<Member> findByLoginIdAndRoleIn(String loginId, List<AccountRole> roles);
}
