package com.example.domain.member.service.command;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.log.enums.LogType;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.support.MemberActivityPublishPort;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.domain.member.support.MemberSocialCleanupPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserMemberCommandServiceTest {

    @InjectMocks
    private UserMemberCommandService userMemberCommandService;

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

    @Test
    @DisplayName("getSupportedRoles는 USER만 반환한다")
    void getSupportedRoles_returns_user() {
        // Act
        final List<AccountRole> roles = userMemberCommandService.getSupportedRoles();

        // Assert
        assertThat(roles).containsExactly(AccountRole.USER);
    }

    @Test
    @DisplayName("createMember에 null을 전달하면 GlobalException이 발생한다")
    void createMember_null_throws_GlobalException() {
        // Act & Assert
        assertThatThrownBy(() -> userMemberCommandService.createMember(null))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("회원 생성 요청 값은 필수입니다.");
    }

    @Test
    @DisplayName("createMember는 역할을 USER로 강제하고 비밀번호를 암호화한다")
    void createMember_forces_USER_role_and_encodes_password() {
        // Arrange
        final MemberCreateCommand command = MemberCreateCommand.of(
                "testUser", "nickname", "rawPassword", AccountRole.ADMIN, MemberType.GENERAL
        );
        final String encodedPassword = "encodedPassword";
        final Member savedMember = Member.from(command.withRole(AccountRole.USER), encodedPassword);

        given(passwordEncoder.encode("rawPassword")).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // Act
        final Long memberId = userMemberCommandService.createMember(command);

        // Assert
        verify(passwordEncoder).encode("rawPassword");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("createMember는 비밀번호를 encode한다")
    void createMember_encodes_password() {
        // Arrange
        final String rawPassword = "myPassword123";
        final String encodedPassword = "encoded_myPassword123";
        final MemberCreateCommand command = MemberCreateCommand.of(
                "loginId", "nick", rawPassword, AccountRole.USER, MemberType.GENERAL
        );
        final Member savedMember = Member.from(command, encodedPassword);

        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // Act
        userMemberCommandService.createMember(command);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("createMember는 활동 로그를 발행한다")
    void createMember_publishes_activity_log() {
        // Arrange
        final MemberCreateCommand command = MemberCreateCommand.of(
                "loginId", "nick", "password", AccountRole.USER, MemberType.GENERAL
        );
        final String encodedPassword = "encodedPw";
        final Member savedMember = Member.from(command, encodedPassword);

        given(passwordEncoder.encode("password")).willReturn(encodedPassword);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // Act
        userMemberCommandService.createMember(command);

        // Assert
        verify(memberActivityPublishPort).publishMemberActivity(
                eq(savedMember.getLoginId()),
                eq(savedMember.getId()),
                eq(LogType.JOIN),
                eq("일반 회원 가입")
        );
    }
}
