package com.example.domain.member.repository;

import com.example.domain.member.entity.MemberImage;
import com.example.domain.member.payload.dto.MemberImageQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberImageRepository extends JpaRepository<MemberImage, Long> {

    @Query("""
            select mi
            from MemberImage mi
            where mi.fileName = :#{#query.fileName}
              and mi.uploadDirect = :#{#query.uploadDirect}
            """)
    Optional<MemberImage> findByFileNameAndUploadDirect(@Param("query") MemberImageQuery query);

}
