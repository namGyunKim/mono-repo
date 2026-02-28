package com.example.domain.social.support;

import com.example.domain.member.entity.Member;

/**
 * Social 도메인이 회원 등록/조회를 요청하는 Port
 *
 * <p>
 * - Social 도메인은 MemberRepository를 직접 참조하지 않습니다.
 * - Port 정의: social/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * - 반환 타입 Member: SocialAccount 엔티티의 @ManyToOne JPA 연관관계에 필요
 * </p>
 */
public interface SocialMemberRegistrationPort {

    /**
     * 소셜 회원을 저장합니다.
     *
     * @return 저장된 Member 엔티티 (SocialAccount JPA 연관관계에 필요하여 엔티티 반환)
     */
    Member saveSocialMember(Member member);

    /**
     * 닉네임 존재 여부를 확인합니다.
     */
    boolean existsByNickName(String nickName);
}
