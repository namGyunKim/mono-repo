package com.example.global.security.blacklist;

import com.example.global.security.blacklist.payload.dto.BlacklistedTokenCleanupCommand;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenHashQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {

    @Query("""
            select case when count(bt) > 0 then true else false end
            from BlacklistedToken bt
            where bt.tokenHash = :#{#query.tokenHash}
            """)
    boolean existsByTokenHash(@Param("query") BlacklistedTokenHashQuery query);

    @Modifying
    @Query("""
            delete from BlacklistedToken bt
            where bt.expiresAt < :#{#command.expiresAt}
            """)
    long deleteExpiredTokens(@Param("command") BlacklistedTokenCleanupCommand command);
}
