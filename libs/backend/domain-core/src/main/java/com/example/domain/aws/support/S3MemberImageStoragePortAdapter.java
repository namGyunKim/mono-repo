package com.example.domain.aws.support;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3ImageDeleteCommand;
import com.example.domain.aws.payload.dto.S3SingleImageDeleteCommand;
import com.example.domain.aws.service.command.S3MemberCommandService;
import com.example.domain.member.enums.MemberUploadDirect;
import com.example.domain.member.payload.dto.MemberImageStorageDeleteCommand;
import com.example.domain.member.payload.dto.MemberImagesStorageDeleteCommand;
import com.example.domain.member.support.MemberImageStoragePort;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3MemberImageStoragePortAdapter implements MemberImageStoragePort {

    private final S3MemberCommandService s3MemberCommandService;

    @Override
    public void deleteImage(MemberImageStorageDeleteCommand command) {
        if (command == null
                || command.memberId() == null
                || command.fileName() == null
                || command.fileName().isBlank()
                || command.uploadDirect() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 저장소 삭제 요청 값이 비어있습니다.");
        }
        s3MemberCommandService.deleteImage(
                S3SingleImageDeleteCommand.of(
                        command.fileName(),
                        resolveImageType(command.uploadDirect()),
                        command.memberId()
                )
        );
    }

    @Override
    public void deleteImages(MemberImagesStorageDeleteCommand command) {
        if (command == null
                || command.memberId() == null
                || command.uploadDirect() == null
                || command.fileNames() == null
                || command.fileNames().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "회원 이미지 저장소 다중 삭제 요청 값이 비어있습니다.");
        }
        s3MemberCommandService.deleteImages(
                S3ImageDeleteCommand.of(
                        command.fileNames(),
                        resolveImageType(command.uploadDirect()),
                        command.memberId()
                )
        );
    }

    private ImageType resolveImageType(MemberUploadDirect uploadDirect) {
        if (uploadDirect == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "이미지 유형은 필수입니다.");
        }
        return switch (uploadDirect) {
            case MEMBER_PROFILE -> ImageType.MEMBER_PROFILE;
        };
    }
}
