package com.example.domain.member.repository;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.global.enums.GlobalActiveEnums;

/**
 * 로그인/프로필 조회용 Projection
 */
public interface LoginMemberViewProjection {

    Long getId();

    String getLoginId();

    AccountRole getRole();

    String getNickName();

    MemberType getMemberType();

    GlobalActiveEnums getActive();
}
