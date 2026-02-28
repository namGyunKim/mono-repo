package com.example.domain.member.entity;

import com.example.domain.member.enums.MemberUploadDirect;
import com.example.global.entity.BaseTimeEntity;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 회원 이미지 엔티티
 *
 * <p>
 * [도메인 불변식]
 * - fileName은 비어있을 수 없습니다.
 * - uploadDirect는 null일 수 없습니다.
 * - member는 null일 수 없습니다 (회원에 종속된 애그리거트 멤버).
 * </p>
 *
 * <p>
 * [애그리거트 관계]
 * - Member가 애그리거트 루트이며, MemberImage는 Member의 생명주기에 종속됩니다.
 * - 외부에서 직접 MemberImage를 생성하지 않고, Member.addMemberImage()를 통해 관리합니다.
 * </p>
 */
@Entity
@Table(name = "member_image", comment = "회원 이미지")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_profile_id", comment = "회원 이미지 아이디")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)", comment = "이미지 경로")
    private MemberUploadDirect uploadDirect;

    @Column(comment = "파일이름")
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private MemberImage(final MemberUploadDirect uploadDirect, final String fileName, final Member member) {
        validateCreation(uploadDirect, fileName, member);
        this.uploadDirect = uploadDirect;
        this.fileName = fileName;
        this.member = member;
    }

    /**
     * MemberImage 생성 팩토리 메서드
     *
     * @param uploadDirect 업로드 경로 (필수)
     * @param fileName     파일명 (필수, 비어있으면 안됨)
     * @param member       소속 회원 (필수)
     * @return 생성된 MemberImage
     * @throws GlobalException 필수 값이 누락된 경우
     */
    public static MemberImage from(
            final MemberUploadDirect uploadDirect,
            final String fileName,
            final Member member
    ) {
        return new MemberImage(uploadDirect, fileName, member);
    }

    /**
     * 프로필 이미지 여부를 반환합니다.
     *
     * @return 프로필 이미지이면 true
     */
    public boolean isProfileImage() {
        return this.uploadDirect == MemberUploadDirect.MEMBER_PROFILE;
    }

    /**
     * 특정 업로드 경로에 해당하는지 확인합니다.
     *
     * @param targetDirect 확인할 업로드 경로
     * @return 일치하면 true
     */
    public boolean matchesUploadDirect(final MemberUploadDirect targetDirect) {
        return this.uploadDirect == targetDirect;
    }

    private void validateCreation(
            final MemberUploadDirect uploadDirect,
            final String fileName,
            final Member member
    ) {
        if (uploadDirect == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "이미지 업로드 경로는 필수입니다.");
        }
        if (!StringUtils.hasText(fileName)) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "파일명은 필수입니다.");
        }
        if (member == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 정보는 필수입니다.");
        }
    }
}
