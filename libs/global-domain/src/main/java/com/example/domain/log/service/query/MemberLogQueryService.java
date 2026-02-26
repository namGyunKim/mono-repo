package com.example.domain.log.service.query;

import com.example.domain.log.payload.dto.MemberLogQuery;
import com.example.domain.log.payload.dto.MemberLogSearchQuery;
import com.example.domain.log.payload.dto.MemberLogView;
import com.example.domain.log.payload.response.MemberLogResponse;
import com.example.domain.log.repository.MemberLogRepository;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberLogQueryService {

    private final MemberLogRepository memberLogRepository;

    /**
     * 회원 활동 로그 목록 조회
     * - 생성일 기준 내림차순 정렬
     * - loginId/memberId/logType/details 조건이 있으면 필터링
     */
    public Page<MemberLogResponse> getMemberLogs(MemberLogQuery query) {
        if (query == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "요청 값이 비어있습니다.");
        }

        final Pageable pageable = PaginationUtils.toPageable(
                query.page(),
                query.size(),
                PaginationUtils.DEFAULT_LOG_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        final Page<MemberLogView> logs = memberLogRepository.search(
                MemberLogSearchQuery.from(query),
                pageable
        );

        return logs.map(MemberLogResponse::from);
    }
}
