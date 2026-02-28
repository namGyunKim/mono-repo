package com.example.domain.member.entity;

import com.example.domain.member.enums.MemberUploadDirect;
import com.example.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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


    protected MemberImage(MemberUploadDirect uploadDirect, String fileName, Member member) {
        this.uploadDirect = uploadDirect;
        this.fileName = fileName;
        this.member = member;
    }

    public static MemberImage from(MemberUploadDirect uploadDirect, String fileName, Member member) {
        return new MemberImage(uploadDirect, fileName, member);
    }
}
