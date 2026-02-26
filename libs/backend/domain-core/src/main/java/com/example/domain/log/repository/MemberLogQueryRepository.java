package com.example.domain.log.repository;

import com.example.domain.log.payload.dto.MemberLogSearchQuery;
import com.example.domain.log.payload.dto.MemberLogView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberLogQueryRepository {

    Page<MemberLogView> search(MemberLogSearchQuery query, Pageable pageable);
}
