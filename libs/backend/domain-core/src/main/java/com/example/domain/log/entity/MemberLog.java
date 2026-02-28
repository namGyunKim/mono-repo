package com.example.domain.log.entity;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.payload.dto.MemberLogCreateCommand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_log", comment = "회원 활동 로그")
@IdClass(MemberLogId.class)
@EntityListeners(value = {AuditingEntityListener.class})
public class MemberLog {

    /**
     * [중요] member_log는 파티셔닝 전환을 위해 최초 생성 후 제거하고,
     * 파티션 테이블로 다시 생성해야 합니다.
     * - 자세한 내용은 docs/db/member_log_partitioning.sql 참고
     * <p>
     * [중요] 파티션은 DB 내부 구현이므로 엔티티를 월별로 추가할 필요가 없습니다.
     * - 엔티티는 항상 부모 테이블(member_log)만 매핑합니다.
     * - 조회/저장은 부모 테이블로 수행하면 DB가 적절한 파티션을 자동 라우팅합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_log_id_seq")
    @SequenceGenerator(
            name = "member_log_id_seq",
            sequenceName = "member_log_id_seq",
            allocationSize = 1
    )
    @Column(name = "log_id", comment = "로그 아이디")
    private Long id;

    @Id
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false, comment = "생성일")
    private LocalDateTime createdAt;

    @Column(comment = "대상 회원 로그인 아이디 (탈퇴 후에도 기록 유지를 위해 문자열 저장)")
    private String loginId;

    @Column(comment = "대상 회원 고유 ID (참조용)")
    private Long memberId;

    // executorId 제거 -> BaseTimeEntity의 createdBy 사용

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)", comment = "활동 유형")
    private LogType logType;

    @Column(length = 500, columnDefinition = "varchar(500)", comment = "상세 내용")
    private String details;

    @Column(comment = "요청 IP")
    private String clientIp;

    @CreatedBy
    @Column(name = "created_by", updatable = false, comment = "생성자")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "modified_at", comment = "수정일")
    private LocalDateTime modifiedAt;

    @LastModifiedBy
    @Column(name = "last_modified_by", comment = "수정자")
    private String lastModifiedBy;

    private MemberLog(String loginId, Long memberId, LogType logType, String details, String clientIp) {
        this.loginId = loginId;
        this.memberId = memberId;
        this.logType = logType;
        this.details = details;
        this.clientIp = clientIp;
    }

    /**
     * 회원 활동 로그 생성 팩토리
     * <p>
     * - 생성자 호출을 외부에 노출하지 않고 의도를 드러내기 위한 표준 생성 메서드입니다.
     * </p>
     */
    public static MemberLog from(MemberLogCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command는 필수입니다.");
        }
        return new MemberLog(
                command.loginId(),
                command.memberId(),
                command.logType(),
                command.details(),
                command.clientIp()
        );
    }
}
