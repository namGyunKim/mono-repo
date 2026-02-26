package com.example.domain.social.google.support;

import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.social.google.payload.response.GoogleUserInfoResponse;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class GoogleSocialMemberCreator {

    private static final int NICKNAME_RANDOM_BOUND = 10000;
    private static final int NICKNAME_MAX_ATTEMPTS = 100;
    private static final String DEFAULT_NICKNAME_PREFIX = "google_user";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member register(GoogleUserInfoResponse userInfo, String socialKey) {
        String loginId = "google_%s".formatted(socialKey);
        String nickName = createUniqueNickName(resolveBaseNickName(userInfo != null ? userInfo.name() : null));

        Member newMember = Member.fromSocial(loginId, nickName, MemberType.GOOGLE);
        newMember.updatePassword(passwordEncoder.encode(createRandomPassword()));

        return memberRepository.save(newMember);
    }

    private String createUniqueNickName(String baseNickName) {
        String nickName = baseNickName;
        int attempts = 0;
        while (memberRepository.existsByNickName(nickName)) {
            attempts++;
            if (attempts >= NICKNAME_MAX_ATTEMPTS) {
                throw new GlobalException(
                        ErrorCode.INTERNAL_SERVER_ERROR,
                        "닉네임 생성에 실패했습니다: base=%s, attempts=%d".formatted(baseNickName, attempts)
                );
            }
            nickName = "%s_%d".formatted(baseNickName, ThreadLocalRandom.current().nextInt(NICKNAME_RANDOM_BOUND));
        }
        return nickName;
    }

    private String resolveBaseNickName(String rawNickName) {
        if (StringUtils.hasText(rawNickName)) {
            return rawNickName.trim();
        }
        return DEFAULT_NICKNAME_PREFIX;
    }

    private String createRandomPassword() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }
}
