package com.example.domain.contract.enums;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberFilterType;
import com.example.domain.member.enums.MemberOrderType;
import com.example.domain.member.enums.MemberType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiEnumSyncTest {

    private static <D extends Enum<D>, A extends Enum<A>> void assertEnumNamesEqual(Class<D> domainType, Class<A> apiType) {
        Set<String> domainNames = Arrays.stream(domainType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> apiNames = Arrays.stream(apiType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        assertEquals(domainNames, apiNames, () -> "Enum constants mismatch: domain=%s, api=%s".formatted(
                domainType.getSimpleName(),
                apiType.getSimpleName()
        ));
    }

    private static <D extends Enum<D>, A extends Enum<A>> void assertRoundTrip(
            D[] domainValues,
            A[] apiValues,
            Function<D, A> fromDomain,
            Function<A, D> toDomain,
            String label
    ) {
        for (D domainValue : domainValues) {
            A apiValue = fromDomain.apply(domainValue);
            D mappedDomainValue = toDomain.apply(apiValue);
            assertEquals(domainValue, mappedDomainValue, () -> "%s domain->api->domain mismatch: %s".formatted(label, domainValue));
        }

        for (A apiValue : apiValues) {
            D domainValue = toDomain.apply(apiValue);
            A mappedApiValue = fromDomain.apply(domainValue);
            assertEquals(apiValue, mappedApiValue, () -> "%s api->domain->api mismatch: %s".formatted(label, apiValue));
        }
    }

    @Test
    void apiEnumNamesMustMatchDomainEnumNames() {
        assertEnumNamesEqual(AccountRole.class, ApiAccountRole.class);
        assertEnumNamesEqual(MemberType.class, ApiMemberType.class);
        assertEnumNamesEqual(MemberActiveStatus.class, ApiMemberActiveStatus.class);
        assertEnumNamesEqual(MemberFilterType.class, ApiMemberFilterType.class);
        assertEnumNamesEqual(MemberOrderType.class, ApiMemberOrderType.class);
        assertEnumNamesEqual(LogType.class, ApiLogType.class);
    }

    @Test
    void apiEnumMappingsMustRoundTrip() {
        assertRoundTrip(
                AccountRole.values(),
                ApiAccountRole.values(),
                ApiAccountRole::fromDomain,
                ApiAccountRole::toDomain,
                "AccountRole"
        );
        assertRoundTrip(
                MemberType.values(),
                ApiMemberType.values(),
                ApiMemberType::fromDomain,
                ApiMemberType::toDomain,
                "MemberType"
        );
        assertRoundTrip(
                MemberActiveStatus.values(),
                ApiMemberActiveStatus.values(),
                ApiMemberActiveStatus::fromDomain,
                ApiMemberActiveStatus::toDomain,
                "MemberActiveStatus"
        );
        assertRoundTrip(
                MemberFilterType.values(),
                ApiMemberFilterType.values(),
                ApiMemberFilterType::fromDomain,
                ApiMemberFilterType::toDomain,
                "MemberFilterType"
        );
        assertRoundTrip(
                MemberOrderType.values(),
                ApiMemberOrderType.values(),
                ApiMemberOrderType::fromDomain,
                ApiMemberOrderType::toDomain,
                "MemberOrderType"
        );
        assertRoundTrip(
                LogType.values(),
                ApiLogType.values(),
                ApiLogType::fromDomain,
                ApiLogType::toDomain,
                "LogType"
        );
    }
}
