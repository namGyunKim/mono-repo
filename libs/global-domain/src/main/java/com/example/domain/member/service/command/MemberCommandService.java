package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberDeactivateCommand;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.payload.dto.MemberUpdateCommand;

import java.util.List;

public interface MemberCommandService {

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

    Long createMember(MemberCreateCommand command);

    Long updateMember(MemberUpdateCommand command);

    Long deactivateMember(MemberDeactivateCommand command);

    void updateMemberRole(MemberRoleUpdateCommand command);

}
