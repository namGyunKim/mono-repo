package com.example.domain.log.repository;

import com.example.domain.log.payload.dto.MemberLogSearchQuery;
import com.example.domain.log.payload.dto.MemberLogView;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.domain.log.entity.QMemberLog.memberLog;

@Repository
@RequiredArgsConstructor
public class MemberLogRepositoryImpl implements MemberLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberLogView> search(MemberLogSearchQuery query, Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("pageable은 필수입니다.");
        }

        final MemberLogSearchQuery safeQuery = query != null
                ? query
                : MemberLogSearchQuery.of("", null, null, "", null, null);
        final Predicate predicate = MemberLogSpecification.search(safeQuery);

        final List<MemberLogView> content = fetchContent(predicate, pageable);
        final long total = fetchTotalCount(predicate);

        return new PageImpl<>(content, pageable, total);
    }

    private List<MemberLogView> fetchContent(Predicate predicate, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(
                        MemberLogView.class,
                        memberLog.id,
                        memberLog.loginId,
                        memberLog.createdBy,
                        memberLog.logType,
                        memberLog.details,
                        memberLog.clientIp,
                        memberLog.createdAt
                ))
                .from(memberLog)
                .where(predicate)
                .orderBy(memberLog.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private long fetchTotalCount(Predicate predicate) {
        final Long total = queryFactory
                .select(memberLog.count())
                .from(memberLog)
                .where(predicate)
                .fetchOne();
        return total != null ? total : 0L;
    }
}
