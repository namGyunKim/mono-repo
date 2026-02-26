package com.example.domain.member.repository;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.global.enums.GlobalActiveEnums;
import com.example.global.enums.GlobalFilterEnums;
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

    private static final Map<GlobalFilterEnums, Function<String, Predicate>> FILTER_PREDICATE_RESOLVERS = Map.of(
            GlobalFilterEnums.LOGIN_ID, member.loginId::containsIgnoreCase,
            GlobalFilterEnums.NICK_NAME, member.nickName::containsIgnoreCase,
            GlobalFilterEnums.ALL, keyword -> member.loginId.containsIgnoreCase(keyword)
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
        if (request.active() != null && request.active() != GlobalActiveEnums.ALL) {
            builder.and(member.active.eq(request.active()));
        }

        // 3. 검색어(SearchWord) 및 필터(Filter) 처리
        resolveSearchPredicate(request.filter(), request.searchWord())
                .ifPresent(builder::and);

        return builder;
    }

    private static Optional<Predicate> resolveSearchPredicate(GlobalFilterEnums filter, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Optional.empty();
        }

        GlobalFilterEnums resolvedFilter = filter != null ? filter : GlobalFilterEnums.ALL;
        Function<String, Predicate> resolver = FILTER_PREDICATE_RESOLVERS.getOrDefault(
                resolvedFilter,
                FILTER_PREDICATE_RESOLVERS.get(GlobalFilterEnums.ALL)
        );

        return Optional.ofNullable(resolver)
                .map(r -> r.apply(keyword));
    }
}
