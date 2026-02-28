package com.example.domain.social.entity;

import com.example.domain.member.entity.Member;
import com.example.domain.social.enums.SocialProvider;
import com.example.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "social_account",
        comment = "소셜 계정",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_social_account_provider_key", columnNames = {"provider", "social_key"}),
                @UniqueConstraint(name = "uk_social_account_member_provider", columnNames = {"member_id", "provider"})
        }
)
public class SocialAccount extends BaseTimeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_account_id", comment = "소셜 계정 아이디")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(30)", nullable = false, comment = "소셜 제공자")
    private SocialProvider provider;

    @Column(name = "social_key", nullable = false, comment = "소셜 제공자 고유 키")
    private String socialKey;

    @Column(name = "refresh_token_encrypted", columnDefinition = "text", comment = "소셜 Refresh Token Encrypted")
    private String refreshTokenEncrypted;

    public static SocialAccount from(Member member, SocialProvider provider, String socialKey) {
        final SocialAccount account = new SocialAccount();
        account.member = member;
        account.provider = provider;
        account.socialKey = socialKey;
        return account;
    }

    public static SocialAccount of(Member member, SocialProvider provider, String socialKey) {
        return from(member, provider, socialKey);
    }

    public void updateRefreshTokenEncrypted(String refreshTokenEncrypted) {
        this.refreshTokenEncrypted = refreshTokenEncrypted;
    }

    public void clearRefreshTokenEncrypted() {
        this.refreshTokenEncrypted = null;
    }
}
