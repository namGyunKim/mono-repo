package com.example.domain.log.repository;

import com.example.domain.log.payload.dto.MemberLogSearchQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.util.StringUtils;

import static com.example.domain.log.entity.QMemberLog.memberLog;

/**
 * QueryDSL 기반 회원 로그 검색 조건 조립
 */
public final class MemberLogSpecification {

    private MemberLogSpecification() {
    }

    public static Predicate search(MemberLogSearchQuery query) {
        final BooleanBuilder builder = new BooleanBuilder();
        if (query == null) {
            return builder;
        }

        if (StringUtils.hasText(query.loginId())) {
            builder.and(memberLog.loginId.containsIgnoreCase(query.loginId()));
        }
        if (query.memberId() != null) {
            builder.and(memberLog.memberId.eq(query.memberId()));
        }
        if (query.logType() != null) {
            builder.and(memberLog.logType.eq(query.logType()));
        }
        if (StringUtils.hasText(query.details())) {
            builder.and(memberLog.details.containsIgnoreCase(query.details()));
        }
        if (query.startAt() != null) {
            builder.and(memberLog.createdAt.goe(query.startAt()));
        }
        if (query.endAt() != null) {
            builder.and(memberLog.createdAt.lt(query.endAt()));
        }

        return builder;
    }
}
