package com.example.domain.member.support;

import com.example.domain.aws.support.MemberImageCommandPort;
import com.example.domain.member.enums.MemberUploadDirect;
import com.example.domain.member.payload.dto.MemberImageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImageRegisterCommand;
import com.example.domain.member.service.command.MemberImageCommandService;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberImageCommandPortAdapter implements MemberImageCommandPort {

    private final MemberImageCommandService memberImageCommandService;

    @Override
    public Long registerProfileImage(Long memberId, String uploadDirect, String fileName) {
        if (memberId == null || uploadDirect == null || fileName == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 등록 요청 값이 비어있습니다.");
        }
        final MemberUploadDirect direct = MemberUploadDirect.valueOf(uploadDirect);
        return memberImageCommandService.registerProfileImage(
                MemberImageRegisterCommand.of(memberId, direct, fileName)
        );
    }

    @Override
    public void deleteProfileImage(Long memberImageId) {
        if (memberImageId == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 삭제 요청 값이 비어있습니다.");
        }
        memberImageCommandService.deleteProfileImage(
                MemberImageDeleteCommand.of(memberImageId)
        );
    }
}
