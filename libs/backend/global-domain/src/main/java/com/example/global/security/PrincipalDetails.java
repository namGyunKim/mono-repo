package com.example.global.security;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.enums.MemberType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/*
 * Spring Security에서 사용하는 인증 객체 구현체입니다.
 * 타임리프에서 sec:authentication="principal.nickName" 처럼
 * 이 클래스의 필드나 Getter 메서드에 직접 접근할 수 있습니다.
 */
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

    // 타임리프에서 principal.nickName으로 호출될 때 사용됩니다.
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
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return authorities;
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
