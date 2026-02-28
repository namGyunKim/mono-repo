package com.example.domain.aws.service.command;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.*;
import com.example.domain.aws.service.common.S3ImageUrlSupport;
import com.example.domain.aws.support.MemberImageCommandPort;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminS3CommandService {

    private static final ImageType DEFAULT_IMAGE_TYPE = ImageType.MEMBER_PROFILE;
    private static final String DEFAULT_UPLOAD_DIRECT = "MEMBER_PROFILE";

    private final S3MemberCommandService s3MemberCommandService;
    private final S3ImageUrlSupport s3ImageUrlSupport;
    private final MemberImageCommandPort memberImageCommandPort;

    public S3ImageUploadResult uploadProfileImage(AdminS3ImageUploadCommand command) {
        validateUploadCommand(command);
        final Long memberId = command.memberId();
        final String uploadedFileName = uploadProfileImageFile(command);
        final Long memberImageId = registerProfileImage(memberId, uploadedFileName);
        final String imageUrl = resolveImageUrl(memberId, uploadedFileName);
        return createUploadResult(memberImageId, uploadedFileName, imageUrl);
    }

    public List<S3ImageUploadResult> uploadProfileImages(AdminS3ImagesUploadCommand command) {
        validateUploadCommand(command);
        final Long memberId = command.memberId();
        final List<String> uploadedFileNames = s3MemberCommandService.uploadImages(
                S3ImagesUploadCommand.of(command.files(), DEFAULT_IMAGE_TYPE, memberId)
        );

        return uploadedFileNames.stream()
                .map(fileName -> registerProfileImageAndCreateUploadResult(memberId, fileName))
                .toList();
    }

    public void deleteProfileImage(AdminS3ImageDeleteCommand command) {
        validateDeleteCommand(command);
        memberImageCommandPort.deleteProfileImage(command.memberImageId());
    }

    private String uploadProfileImageFile(AdminS3ImageUploadCommand command) {
        return s3MemberCommandService.uploadImage(
                S3ImageUploadCommand.of(command.file(), DEFAULT_IMAGE_TYPE, command.memberId())
        );
    }

    private S3ImageUploadResult registerProfileImageAndCreateUploadResult(Long memberId, String uploadedFileName) {
        final Long memberImageId = registerProfileImage(memberId, uploadedFileName);
        final String imageUrl = resolveImageUrl(memberId, uploadedFileName);
        return createUploadResult(memberImageId, uploadedFileName, imageUrl);
    }

    private Long registerProfileImage(Long memberId, String uploadedFileName) {
        return memberImageCommandPort.registerProfileImage(memberId, DEFAULT_UPLOAD_DIRECT, uploadedFileName);
    }

    private String resolveImageUrl(Long memberId, String uploadedFileName) {
        return s3ImageUrlSupport.resolve(
                S3ImageUrlQuery.of(uploadedFileName, DEFAULT_IMAGE_TYPE, memberId)
        );
    }

    private S3ImageUploadResult createUploadResult(Long memberImageId, String uploadedFileName, String imageUrl) {
        return S3ImageUploadResult.of(memberImageId, uploadedFileName, imageUrl);
    }

    private void validateUploadCommand(AdminS3ImageUploadCommand command) {
        if (command == null || command.memberId() == null || command.memberId() <= 0 || command.file() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 업로드 요청 값이 비어있습니다.");
        }
    }

    private void validateUploadCommand(AdminS3ImagesUploadCommand command) {
        if (command == null || command.memberId() == null || command.memberId() <= 0 || command.files() == null || command.files().isEmpty()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 다중 업로드 요청 값이 비어있습니다.");
        }
    }

    private void validateDeleteCommand(AdminS3ImageDeleteCommand command) {
        if (command == null || command.memberImageId() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 삭제 요청 값이 비어있습니다.");
        }
    }
}
