package com.example.domain.member.entity;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.dto.MemberCreateCommand;
import com.example.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member", comment = "회원")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", comment = "유저 아이디")
    private Long id;

    @Column(unique = true, comment = "유저 로그인 아이디") // 변경 가능하도록 updatable = false 제거 (탈퇴 시 변경 위해)
    private String loginId;

    @Column(unique = true, comment = "유저 닉네임")
    private String nickName;

    @Column(comment = "유저 비밀번호")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)", comment = "유저 활성 상태")
    private MemberActiveStatus active;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)", comment = "유저 권한")
    private AccountRole role;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)", comment = "유저 타입")
    private MemberType memberType;

    @Column(name = "jwt_refresh_token_encrypted", columnDefinition = "text", comment = "JWT Refresh Token Encrypted")
    private String refreshTokenEncrypted;

    @Column(nullable = false, comment = "JWT 토큰 버전")
    private long tokenVersion = 0L;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    // [중요]
    // JPA 엔티티의 컬렉션 필드는 `final` / `transient` 키워드를 사용하지 않는 것이 안전합니다.
    // - final: Hibernate가 PersistentCollection으로 교체/주입할 수 없어 런타임 문제가 발생할 수 있습니다.
    // - transient: JPA 스펙상 영속 대상에서 제외될 수 있어 매핑 누락/동작 불일치가 생길 수 있습니다.
    private List<MemberImage> memberImages = new ArrayList<>();

    // 생성자: 소셜 로그인용
    private Member(String loginId, String nickName, MemberType memberType) {
        this.loginId = loginId;
        this.nickName = nickName;
        this.role = AccountRole.USER;
        this.active = MemberActiveStatus.ACTIVE;
        this.memberType = memberType;
    }

    /**
     * 로컬(비소셜) 계정 생성 전용 팩토리
     * - 비밀번호는 반드시 "암호화된 값"을 전달해야 합니다.
     *
     * <p>
     * [네이밍 표준]
     * - from(...): 외부 입력(command) 기반으로 엔티티를 구성하는 경우
     * </p>
     */
    public static Member from(MemberCreateCommand command, String encodedPassword) {
        Member member = new Member();
        member.loginId = command.loginId();
        member.nickName = command.nickName();
        member.password = encodedPassword;
        member.role = command.role() != null ? command.role() : AccountRole.USER;
        member.active = MemberActiveStatus.ACTIVE;
        member.memberType = command.memberType();
        return member;
    }

    /**
     * 소셜 로그인 전용 생성 팩토리
     * <p>
     * - 소셜 가입 시 공통 필드를 일관되게 초기화하기 위한 표준 생성 경로입니다.
     * </p>
     */
    public static Member fromSocial(String loginId, String nickName, MemberType memberType) {
        return new Member(loginId, nickName, memberType);
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void addMemberImage(MemberImage memberImage) {
        if (memberImage == null) {
            return;
        }
        this.memberImages.add(memberImage);
    }

    public void removeMemberImage(MemberImage memberImage) {
        if (memberImage == null) {
            return;
        }
        this.memberImages.remove(memberImage);
    }

    // 더티 체킹을 위한 회원 정보 수정
    public void changeNickName(String nickName) {
        this.nickName = nickName;
    }

    // 회원 탈퇴 처리 (Soft Delete + Unique Key 회피)
    public void withdraw() {
        String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        this.active = MemberActiveStatus.INACTIVE;
        this.loginId = this.loginId + "_LEAVE_" + nowStr;
        this.nickName = this.nickName + "_LEAVE_" + nowStr;
        this.refreshTokenEncrypted = null;
        rotateTokenVersion();
    }

    public void updateRefreshTokenEncrypted(String refreshTokenEncrypted) {
        this.refreshTokenEncrypted = refreshTokenEncrypted;
    }

    public void invalidateRefreshTokenEncrypted() {
        this.refreshTokenEncrypted = null;
    }

    public void rotateTokenVersion() {
        this.tokenVersion += 1;
    }

    // Active 상태 변경
    public void changeActive(MemberActiveStatus active) {
        this.active = active;
    }

    public void changeRole(AccountRole newRole) {
        this.role = newRole;
    }
}
