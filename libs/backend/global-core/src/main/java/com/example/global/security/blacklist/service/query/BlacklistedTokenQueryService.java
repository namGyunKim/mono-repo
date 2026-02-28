package com.example.global.security.blacklist.service.query;

import com.example.global.security.blacklist.payload.dto.BlacklistedTokenCheckQuery;
import com.example.global.security.blacklist.support.BlacklistedTokenChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenQueryService {

    private final BlacklistedTokenChecker blacklistedTokenChecker;

    @Transactional(readOnly = true)
    public boolean isBlacklisted(final BlacklistedTokenCheckQuery query) {
        if (query == null || !StringUtils.hasText(query.token())) {
            return false;
        }

        return blacklistedTokenChecker.isBlacklisted(query.token());
    }
}
