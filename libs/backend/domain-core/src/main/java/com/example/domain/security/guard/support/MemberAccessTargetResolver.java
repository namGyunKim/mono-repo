package com.example.domain.security.guard.support;

import com.example.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberAccessTargetResolver {

    private final MemberRepository memberRepository;

    public Optional<MemberAccessTarget> resolve(Long targetId) {
        if (targetId == null || targetId <= 0) {
            return Optional.empty();
        }

        return memberRepository.findById(targetId)
                .map(member -> MemberAccessTarget.of(member.getRole(), member.getId(), member.getActive()));
    }
}
