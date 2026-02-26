package com.example.domain.social.repository;

import com.example.domain.social.entity.SocialAccount;
import com.example.domain.social.payload.dto.SocialAccountKeyQuery;
import com.example.domain.social.payload.dto.SocialAccountMemberProviderQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    @Query("""
            select sa
            from SocialAccount sa
            where sa.provider = :#{#query.provider}
              and sa.socialKey = :#{#query.socialKey}
            """)
    Optional<SocialAccount> findByProviderAndSocialKey(@Param("query") SocialAccountKeyQuery query);

    @Query("""
            select sa
            from SocialAccount sa
            where sa.member.id = :#{#query.memberId}
              and sa.provider = :#{#query.provider}
            """)
    Optional<SocialAccount> findByMemberIdAndProvider(@Param("query") SocialAccountMemberProviderQuery query);

    void deleteByMemberId(Long memberId);
}
