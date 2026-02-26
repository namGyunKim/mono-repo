package com.example.domain.member.service.command;

import com.example.domain.member.entity.Member;
import com.example.domain.member.entity.MemberImage;
import com.example.domain.member.payload.dto.MemberImageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImageRegisterCommand;
import com.example.domain.member.payload.dto.MemberImageStorageDeleteCommand;
import com.example.domain.member.repository.MemberImageRepository;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberImageCommandService {

    private final MemberRepository memberRepository;
    private final MemberImageRepository memberImageRepository;
    private final MemberImageStoragePort memberImageStoragePort;

    public Long registerProfileImage(MemberImageRegisterCommand command) {
        if (command == null || command.memberId() == null || command.uploadDirect() == null || command.fileName() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 등록 요청 값이 비어있습니다.");
        }

        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        MemberImage image = MemberImage.from(command.uploadDirect(), command.fileName(), member);
        member.addMemberImage(image);
        memberRepository.flush();

        return image.getId();
    }

    public void deleteProfileImage(MemberImageDeleteCommand command) {
        if (command == null || command.memberImageId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 삭제 요청 값이 비어있습니다.");
        }

        MemberImage image = memberImageRepository.findById(command.memberImageId())
                .orElseThrow(() -> new GlobalException(ErrorCode.FILE_NOT_FOUND));

        Member member = image.getMember();
        if (member == null) {
            throw new GlobalException(ErrorCode.MEMBER_NOT_EXIST);
        }

        memberImageStoragePort.deleteImage(
                MemberImageStorageDeleteCommand.of(member.getId(), image.getFileName(), image.getUploadDirect())
        );

        member.removeMemberImage(image);
        memberImageRepository.delete(image);
    }
}
