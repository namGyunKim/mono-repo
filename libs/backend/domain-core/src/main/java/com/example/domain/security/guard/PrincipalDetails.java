package com.example.domain.security.guard;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.enums.MemberType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/* Spring Security 인증 주체 구현체 */
public class PrincipalDetails implements UserDetails {

    private final Long id;
    private final String loginId;
    private final String password;
    private final String nickName;
    private final AccountRole role;
    private final MemberType memberType;

    public PrincipalDetails(AccountAuthMemberView member) {
        if (member == null) {
            throw new IllegalArgumentException("member는 필수입니다.");
        }
        this.id = member.id();
        this.loginId = member.loginId();
        this.password = member.password();
        this.nickName = member.nickName();
        this.role = member.role();
        this.memberType = member.memberType();
    }

    public String getNickName() {
        return nickName;
    }

    public Long getId() {
        return id;
    }

    public AccountRole getRole() {
        return role;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 필요하다면 active 상태 값을 별도 보관해 여기에서 함께 검증할 수 있습니다.
        return true;
    }
}
