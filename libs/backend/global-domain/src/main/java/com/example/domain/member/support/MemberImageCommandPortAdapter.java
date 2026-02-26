package com.example.domain.member.support;

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
    public Long registerProfileImage(MemberImageRegisterCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 등록 요청 값이 비어있습니다.");
        }
        return memberImageCommandService.registerProfileImage(command);
    }

    @Override
    public void deleteProfileImage(MemberImageDeleteCommand command) {
        if (command == null || command.memberImageId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 삭제 요청 값이 비어있습니다.");
        }
        memberImageCommandService.deleteProfileImage(command);
    }
}
