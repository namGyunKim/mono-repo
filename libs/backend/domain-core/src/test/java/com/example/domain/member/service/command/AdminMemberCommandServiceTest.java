package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.payload.dto.MemberRoleUpdateCommand;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.support.MemberActivityPublishPort;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberPermissionCheckPort;
import com.example.domain.member.support.MemberSocialCleanupPort;
import com.example.domain.member.support.MemberTokenRevocationPort;
import com.example.domain.member.support.MemberUniquenessSupport;
import com.example.global.exception.GlobalException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminMemberCommandServiceTest {

    @InjectMocks
    private AdminMemberCommandService adminMemberCommandService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberUniquenessSupport memberUniquenessSupport;

    @Mock
    private MemberImageStoragePort memberImageStoragePort;

    @Mock
    private MemberSocialCleanupPort memberSocialCleanupPort;

    @Mock
    private MemberActivityPublishPort memberActivityPublishPort;

    @Mock
    private MemberTokenRevocationPort memberTokenRevocationPort;

    @Mock
    private MemberPermissionCheckPort memberPermissionCheckPort;

    @Test
    @DisplayName("getSupportedRoles는 ADMIN, SUPER_ADMIN을 반환한다")
    void getSupportedRoles_returns_admin_and_super_admin() {
        // Act
        final List<AccountRole> roles = adminMemberCommandService.getSupportedRoles();

        // Assert
        assertThat(roles).containsExactly(AccountRole.ADMIN, AccountRole.SUPER_ADMIN);
    }

    @Test
    @DisplayName("createMember에 null을 전달하면 GlobalException이 발생한다")
    void createMember_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> adminMemberCommandService.createMember(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("회원 생성 요청 값은 필수입니다.");
    }

    @Test
    @DisplayName("createMember에 USER 역할을 전달하면 GlobalException이 발생한다")
    void createMember_USER_role_throws_GlobalException() {
        // Arrange
        final MemberCreateCommand command = MemberCreateCommand.of(
                "admin", "adminNick", "password", AccountRole.USER, MemberType.GENERAL
        );

        // Act & Assert
        assertThatThrownBy(() -> adminMemberCommandService.createMember(command))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("ADMIN 또는 SUPER_ADMIN 권한만 허용됩니다.");
    }

    @Test
    @DisplayName("createMember에 ADMIN 역할을 전달하면 정상 생성된다")
    void createMember_ADMIN_role_succeeds() {
        // Arrange
        final MemberCreateCommand command = MemberCreateCommand.of(
                "admin", "adminNick", "password", AccountRole.ADMIN, MemberType.GENERAL
        );
        final String encodedPassword = "encodedPw";
        final Member savedMember = Member.from(command, encodedPassword);

        given(passwordEncoder.encode("password")).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // Act
        final Long memberId = adminMemberCommandService.createMember(command);

        // Assert
        verify(passwordEncoder).encode("password");
        verify(memberRepository).save(any(Member.class));
        verify(memberActivityPublishPort).publishMemberActivity(
                anyString(), any(), eq(LogType.JOIN), eq("관리자 계정 생성")
        );
    }

    @Test
    @DisplayName("createMember에 SUPER_ADMIN 역할을 전달하면 정상 생성된다")
    void createMember_SUPER_ADMIN_role_succeeds() {
        // Arrange
        final MemberCreateCommand command = MemberCreateCommand.of(
                "superAdmin", "superNick", "password", AccountRole.SUPER_ADMIN, MemberType.GENERAL
        );
        final String encodedPassword = "encodedPw";
        final Member savedMember = Member.from(command, encodedPassword);

        given(passwordEncoder.encode("password")).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // Act
        final Long memberId = adminMemberCommandService.createMember(command);

        // Assert
        verify(passwordEncoder).encode("password");
        verify(memberRepository).save(any(Member.class));
        verify(memberActivityPublishPort).publishMemberActivity(
                anyString(), any(), eq(LogType.JOIN), eq("최고 관리자 계정 생성")
        );
    }

    @Test
    @DisplayName("updateMemberRole에서 자기 자신의 등급 변경 시 GlobalException이 발생한다")
    void updateMemberRole_self_change_throws_GlobalException() {
        // Arrange
        final Long memberId = 1L;
        final MemberRoleUpdateCommand command = MemberRoleUpdateCommand.of(memberId, AccountRole.ADMIN);
        given(memberPermissionCheckPort.isSameMember(memberId)).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> adminMemberCommandService.updateMemberRole(command))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("자신의 등급은 변경할 수 없습니다.");
    }
}
