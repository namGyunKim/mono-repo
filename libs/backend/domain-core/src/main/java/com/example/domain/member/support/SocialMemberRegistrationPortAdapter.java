package com.example.domain.member.support;

import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.social.support.SocialMemberRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SocialMemberRegistrationPort 어댑터 — Member 도메인이 제공
 */
@Component
@RequiredArgsConstructor
public class SocialMemberRegistrationPortAdapter implements SocialMemberRegistrationPort {

    private final MemberRepository memberRepository;

    @Override
    public Member saveSocialMember(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public boolean existsByNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }
}
