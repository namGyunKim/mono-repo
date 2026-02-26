package com.example.global.security.service.query;

import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.payload.dto.MemberLoginIdQuery;
import com.example.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberAuthQueryService {

    private final MemberRepository memberRepository;

    public Optional<Member> findActiveMemberForAuthentication(MemberLoginIdQuery query) {
        if (!isValidQuery(query)) {
            return Optional.empty();
        }

        return memberRepository.findByLoginId(query.loginId())
                .filter(member -> member.getActive() == MemberActiveStatus.ACTIVE);
    }

    public Optional<Long> findMemberIdByLoginId(MemberLoginIdQuery query) {
        if (!isValidQuery(query)) {
            return Optional.empty();
        }

        return memberRepository.findByLoginId(query.loginId())
                .map(Member::getId);
    }

    private boolean isValidQuery(MemberLoginIdQuery query) {
        return query != null && StringUtils.hasText(query.loginId());
    }
}
