package com.example.domain.member.service;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.repository.MemberRepository;
import com.example.global.exception.GlobalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberStrategyFactoryTest {

    private MemberStrategyFactory memberStrategyFactory;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // init()을 호출하지 않으므로 내부 commandServiceMap/queryServiceMap은 비어 있다
        memberStrategyFactory = new MemberStrategyFactory(applicationContext, memberRepository);
    }

    @Test
    @DisplayName("getCommandService에 null role을 전달하면 GlobalException이 발생한다")
    void getCommandService_null_role_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getCommandService(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("role은 필수입니다.");
    }

    @Test
    @DisplayName("getCommandService에 등록되지 않은 role을 전달하면 GlobalException이 발생한다")
    void getCommandService_unregistered_role_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getCommandService(AccountRole.USER))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("지원하지 않는 권한 타입입니다(Command)");
    }

    @Test
    @DisplayName("getQueryService에 null role을 전달하면 GlobalException이 발생한다")
    void getQueryService_null_role_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getQueryService(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("role은 필수입니다.");
    }

    @Test
    @DisplayName("getQueryService에 등록되지 않은 role을 전달하면 GlobalException이 발생한다")
    void getQueryService_unregistered_role_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getQueryService(AccountRole.USER))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("지원하지 않는 권한 타입입니다(Query)");
    }

    @Test
    @DisplayName("getCommandServiceByMemberId에 null memberId를 전달하면 GlobalException이 발생한다")
    void getCommandServiceByMemberId_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getCommandServiceByMemberId(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("memberId는 필수입니다.");
    }

    @Test
    @DisplayName("getQueryServiceByMemberId에 null memberId를 전달하면 GlobalException이 발생한다")
    void getQueryServiceByMemberId_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getQueryServiceByMemberId(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("memberId는 필수입니다.");
    }

    @Test
    @DisplayName("getCommandServiceByMemberId에 존재하지 않는 memberId를 전달하면 GlobalException이 발생한다")
    void getCommandServiceByMemberId_not_found_throws_GlobalException() {
        // Arrange
        final Long memberId = 999L;
        given(memberRepository.findById(memberId)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> memberStrategyFactory.getCommandServiceByMemberId(memberId))
                .isInstanceOf(GlobalException.class);
    }
}
