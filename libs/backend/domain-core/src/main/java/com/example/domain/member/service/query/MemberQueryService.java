package com.example.domain.member.service.query;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.dto.MemberDetailQuery;
import com.example.domain.member.payload.dto.MemberListQuery;
import com.example.domain.member.payload.dto.MemberRoleExistsQuery;
import com.example.domain.member.payload.response.DetailMemberResponse;
import com.example.domain.member.payload.response.MemberListResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MemberQueryService {

    /**
     * 전략 패턴(Factory)에서 역할별 구현체를 자동 등록하기 위해, 서비스가 지원하는 권한 목록을 반환합니다.
     *
     * <p>
     * [주의]
     * - AOP 프록시(JDK Dynamic Proxy) 환경에서도 안전하게 동작하도록
     * "인터페이스"에 메서드를 정의합니다.
     * </p>
     */
    List<AccountRole> getSupportedRoles();

    boolean existsByRole(MemberRoleExistsQuery query);

    Page<MemberListResponse> getList(MemberListQuery memberUserListQuery);

    DetailMemberResponse getDetail(MemberDetailQuery query);
}
