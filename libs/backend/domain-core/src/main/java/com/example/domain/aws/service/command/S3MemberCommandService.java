package com.example.domain.aws.service.command;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3ImageDeleteCommand;
import com.example.domain.aws.service.common.S3BucketResolver;
import com.example.domain.aws.service.common.S3CloneSupport;
import com.example.domain.aws.service.common.S3DeleteSupport;
import com.example.domain.aws.service.common.S3KeyBuilder;
import com.example.domain.aws.service.common.S3UploadSupport;
import com.example.domain.aws.service.common.S3UrlParser;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Transactional
public class S3MemberCommandService extends AbstractS3CommandService {

    public S3MemberCommandService(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser,
            S3UploadSupport s3UploadSupport,
            S3DeleteSupport s3DeleteSupport,
            S3CloneSupport s3CloneSupport
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser, s3UploadSupport, s3DeleteSupport, s3CloneSupport);
    }

    @Override
    protected void validateImageType(ImageType imageType) {
        ImageType.validateMemberUploadType(imageType);
    }

    @Override
    public void deleteImages(S3ImageDeleteCommand command) {
        if (command == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 삭제 요청 값은 필수입니다.");
        }
        super.deleteImages(command);
    }

}
