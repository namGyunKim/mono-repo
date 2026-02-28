package com.example.domain.member.support;

import com.example.domain.member.payload.dto.MemberLoginIdDuplicateCheckQuery;
import com.example.domain.member.payload.dto.MemberNickNameExclusiveDuplicateCheckQuery;
import com.example.domain.member.payload.dto.MemberNickNameDuplicateCheckQuery;
import com.example.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 회원의 Unique 제약(로그인 아이디/닉네임 등) 관련 검증을 한 곳에서 제공하기 위한 컴포넌트입니다.
 * <p>
 * [의도]
 * - Validator나 CommandService에서 중복 검사 로직이 흩어지면, 중복 코드가 늘고 정책이 분산될 수 있습니다.
 * - 요청(PathVariable/세션)에 의존하지 않는 순수 검증은 도메인 support 계층으로 중앙화하여 재사용합니다.
 */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberUniquenessSupport {

    private final MemberRepository memberRepository;

    public boolean isLoginIdDuplicated(MemberLoginIdDuplicateCheckQuery query) {
        return query != null
                && StringUtils.hasText(query.loginId())
                && memberRepository.existsByLoginId(query.loginId());
    }

    public boolean isNickNameDuplicated(MemberNickNameDuplicateCheckQuery query) {
        return query != null
                && StringUtils.hasText(query.nickName())
                && memberRepository.existsByNickName(query.nickName());
    }

    /**
     * 특정 회원(loginId)을 제외하고 닉네임 중복 여부를 확인합니다.
     */
    public boolean isNickNameDuplicatedExceptLoginId(MemberNickNameExclusiveDuplicateCheckQuery command) {
        if (command == null || !StringUtils.hasText(command.nickName()) || !StringUtils.hasText(command.excludedLoginId())) {
            return false;
        }
        return memberRepository.existsByNickNameAndLoginIdNot(command.nickName(), command.excludedLoginId());
    }
}
