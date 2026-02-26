package com.example.domain.aws.service.command;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.*;
import com.example.domain.aws.service.common.*;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;
import java.util.stream.Collectors;

/**
 * S3 Command 공통 로직.
 * [성능 최적화] InputStream 기반 업로드 및 DeleteObjects를 이용한 일괄 삭제 적용.
 */
public abstract class AbstractS3CommandService extends AbstractS3ServiceSupport implements S3CommandService {

    private final S3UploadSupport s3UploadSupport;
    private final S3DeleteSupport s3DeleteSupport;
    private final S3CloneSupport s3CloneSupport;

    protected AbstractS3CommandService(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser,
            S3UploadSupport s3UploadSupport,
            S3DeleteSupport s3DeleteSupport,
            S3CloneSupport s3CloneSupport
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
        this.s3UploadSupport = s3UploadSupport;
        this.s3DeleteSupport = s3DeleteSupport;
        this.s3CloneSupport = s3CloneSupport;
    }

    protected abstract void validateImageType(ImageType imageType);

    @Override
    public String uploadImage(S3ImageUploadCommand command) {
        validateUploadCommand(command);
        MultipartFile file = command.file();
        ImageType imageType = command.imageType();
        Long entityId = command.entityId();
        validateImageType(imageType);
        return s3UploadSupport.upload(file, imageType, entityId);
    }

    @Override
    public List<String> uploadImages(S3ImagesUploadCommand command) {
        validateUploadCommand(command);
        return command.files().stream()
                .map(file -> uploadImage(S3ImageUploadCommand.of(file, command.imageType(), command.entityId())))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteImage(S3SingleImageDeleteCommand command) {
        validateDeleteCommand(command);
        s3DeleteSupport.deleteSingle(command.fileName(), command.imageType(), command.entityId());
    }

    @Override
    public void deleteImages(S3ImageDeleteCommand command) {
        validateDeleteCommand(command);
        s3DeleteSupport.deleteBatch(command.fileNames(), command.imageType(), command.entityId());
    }

    @Override
    public String cloneImageFromUrl(S3ImageCloneCommand command) {
        validateCloneCommand(command);
        ImageType destinationImageType = command.destinationImageType();
        Long destinationEntityId = command.destinationEntityId();
        validateImageType(destinationImageType);
        return s3CloneSupport.cloneFromUrl(command.sourceS3Url(), destinationImageType, destinationEntityId);
    }

    @Override
    public List<String> cloneImagesFromUrls(S3ImagesCloneCommand command) {
        validateCloneCommand(command);
        return command.sourceS3Urls().stream()
                .map(url -> cloneImageFromUrl(S3ImageCloneCommand.of(
                        url,
                        command.destinationImageType(),
                        command.destinationEntityId()
                )))
                .collect(Collectors.toList());
    }

    private void validateUploadCommand(S3ImageUploadCommand command) {
        if (command == null || command.file() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 업로드 요청 값이 비어있습니다.");
        }
    }

    private void validateUploadCommand(S3ImagesUploadCommand command) {
        if (command == null || command.files() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 다중 업로드 요청 값이 비어있습니다.");
        }
    }

    private void validateDeleteCommand(S3SingleImageDeleteCommand command) {
        if (command == null
                || command.fileName() == null
                || command.fileName().isBlank()
                || command.imageType() == null
                || command.entityId() == null
                || command.entityId() <= 0) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 삭제 요청 값이 비어있습니다.");
        }
    }

    private void validateDeleteCommand(S3ImageDeleteCommand command) {
        if (command == null
                || command.fileNames() == null
                || command.fileNames().isEmpty()
                || command.fileNames().stream().anyMatch(fileName -> fileName == null || fileName.isBlank())
                || command.imageType() == null
                || command.entityId() == null
                || command.entityId() <= 0) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 삭제 요청 값이 비어있습니다.");
        }
    }

    private void validateCloneCommand(S3ImageCloneCommand command) {
        if (command == null || command.sourceS3Url() == null || command.sourceS3Url().isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 복사 요청 값이 비어있습니다.");
        }
    }

    private void validateCloneCommand(S3ImagesCloneCommand command) {
        if (command == null || command.sourceS3Urls() == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 다중 이미지 복사 요청 값이 비어있습니다.");
        }
    }
}
