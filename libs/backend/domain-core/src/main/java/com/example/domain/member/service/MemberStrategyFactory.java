package com.example.domain.member.service;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.service.command.MemberCommandService;
import com.example.domain.member.service.query.MemberQueryService;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberStrategyFactory {

    private final ApplicationContext applicationContext;
    private final MemberRepository memberRepository;

    // 전략 패턴을 위한 Map (키: AccountRole, 값: 서비스)
    private final Map<AccountRole, MemberCommandService> commandServiceMap = new EnumMap<>(AccountRole.class);
    private final Map<AccountRole, MemberQueryService> queryServiceMap = new EnumMap<>(AccountRole.class);

    @PostConstruct
    public void init() {
        initializeCommandServices();
        initializeQueryServices();
        log.info(
                "MemberStrategyFactory 초기화 완료. Command 서비스 수: {}, Query 서비스 수: {}",
                commandServiceMap.size(),
                queryServiceMap.size()
        );
    }

    private void initializeCommandServices() {
        final Map<String, MemberCommandService> beans = applicationContext.getBeansOfType(MemberCommandService.class);

        for (MemberCommandService service : beans.values()) {
            // [중요]
            // - 서비스가 @Transactional 등으로 프록시(JDK Dynamic Proxy)로 감싸질 수 있습니다.
            // - 프록시 객체는 구체 클래스(AbstractMemberCommandService)의 인스턴스가 아닐 수 있으므로
            //   instanceof 기반 분기 대신, 인터페이스에 정의된 getSupportedRoles()를 사용합니다.
            final List<AccountRole> supportedRoles = service.getSupportedRoles();
            if (supportedRoles == null || supportedRoles.isEmpty()) {
                log.warn("MemberCommandService 구현체가 지원 Role을 반환하지 않습니다: {}", service.getClass().getName());
                continue;
            }

            registerForRoles(service, supportedRoles, commandServiceMap, "MemberCommandService");
        }
    }

    private void initializeQueryServices() {
        final Map<String, MemberQueryService> beans = applicationContext.getBeansOfType(MemberQueryService.class);

        for (MemberQueryService service : beans.values()) {
            // Command와 동일한 이유로, 프록시 안정성을 위해 인터페이스 메서드 사용
            final List<AccountRole> supportedRoles = service.getSupportedRoles();
            if (supportedRoles == null || supportedRoles.isEmpty()) {
                log.warn("MemberQueryService 구현체가 지원 Role을 반환하지 않습니다: {}", service.getClass().getName());
                continue;
            }

            registerForRoles(service, supportedRoles, queryServiceMap, "MemberQueryService");
        }
    }

    private <T> void registerForRoles(T service, List<AccountRole> roles, Map<AccountRole, T> serviceMap, String serviceTypeName) {
        for (AccountRole role : roles) {
            final T existing = serviceMap.put(role, service);
            if (existing != null && existing != service) {
                throw new IllegalStateException("%s 중복 등록: role=%s".formatted(serviceTypeName, role));
            }
        }
    }

    /**
     * AccountRole에 맞는 Command 서비스를 반환
     */
    public MemberCommandService getCommandService(AccountRole role) {
        if (role == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "role은 필수입니다.");
        }
        final MemberCommandService service = commandServiceMap.get(role);
        if (service == null) {
            throw new GlobalException(ErrorCode.INPUT_VALUE_INVALID, "지원하지 않는 권한 타입입니다(Command): %s".formatted(role));
        }
        return service;
    }

    /**
     * 회원 ID 기준으로 Command 서비스를 반환
     */
    public MemberCommandService getCommandServiceByMemberId(Long memberId) {
        return getCommandService(resolveRoleByMemberId(memberId));
    }

    /**
     * AccountRole에 맞는 Query 서비스를 반환
     */
    public MemberQueryService getQueryService(AccountRole role) {
        if (role == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "role은 필수입니다.");
        }
        final MemberQueryService service = queryServiceMap.get(role);
        if (service == null) {
            throw new GlobalException(ErrorCode.INPUT_VALUE_INVALID, "지원하지 않는 권한 타입입니다(Query): %s".formatted(role));
        }
        return service;
    }

    /**
     * 회원 ID 기준으로 Query 서비스를 반환
     */
    public MemberQueryService getQueryServiceByMemberId(Long memberId) {
        return getQueryService(resolveRoleByMemberId(memberId));
    }

    private AccountRole resolveRoleByMemberId(Long memberId) {
        if (memberId == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "memberId는 필수입니다.");
        }

        return memberRepository.findById(memberId)
                .map(Member::getRole)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));
    }
}
