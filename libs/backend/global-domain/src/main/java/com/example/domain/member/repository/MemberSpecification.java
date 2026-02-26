package com.example.domain.member.repository;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.example.domain.member.entity.QMember.member;

/**
 * QueryDSL을 이용한 동적 쿼리 조건(Predicate) 생성
 * 기존 JPA Specification을 대체합니다.
 */
public final class MemberSpecification {

    private static final Map<MemberFilterType, Function<String, Predicate>> FILTER_PREDICATE_RESOLVERS = Map.of(
            MemberFilterType.LOGIN_ID, member.loginId::containsIgnoreCase,
            MemberFilterType.NICK_NAME, member.nickName::containsIgnoreCase,
            MemberFilterType.ALL, keyword -> member.loginId.containsIgnoreCase(keyword)
                    .or(member.nickName.containsIgnoreCase(keyword))
    );

    private MemberSpecification() {
    }

    public static Predicate searchMember(MemberListQuery request, List<AccountRole> roles) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. 권한(Role) 필터링 (IN 절)
        if (roles != null && !roles.isEmpty()) {
            builder.and(member.role.in(roles));
        }

        // 2. 활성화 상태(Active) 필터링
        if (request.active() != null && request.active() != MemberActiveStatus.ALL) {
            builder.and(member.active.eq(request.active()));
        }

        // 3. 검색어(SearchWord) 및 필터(Filter) 처리
        resolveSearchPredicate(request.filter(), request.searchWord())
                .ifPresent(builder::and);

        return builder;
    }

    private static Optional<Predicate> resolveSearchPredicate(MemberFilterType filter, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Optional.empty();
        }

        MemberFilterType resolvedFilter = filter != null ? filter : MemberFilterType.ALL;
        Function<String, Predicate> resolver = FILTER_PREDICATE_RESOLVERS.getOrDefault(
                resolvedFilter,
                FILTER_PREDICATE_RESOLVERS.get(MemberFilterType.ALL)
        );

        return Optional.ofNullable(resolver)
                .map(r -> r.apply(keyword));
    }
}
