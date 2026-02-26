package com.example.domain.log.repository;

import com.example.domain.log.entity.MemberLog;
import com.example.domain.log.entity.MemberLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberLogRepository extends JpaRepository<MemberLog, MemberLogId>, MemberLogQueryRepository {
}
