package com.example.domain.aws.service.command;

import com.example.domain.aws.payload.dto.S3ImageCloneCommand;
import com.example.domain.aws.payload.dto.S3ImageDeleteCommand;
import com.example.domain.aws.payload.dto.S3ImageUploadCommand;
import com.example.domain.aws.payload.dto.S3ImagesCloneCommand;
import com.example.domain.aws.payload.dto.S3ImagesUploadCommand;
import com.example.domain.aws.payload.dto.S3SingleImageDeleteCommand;

import java.util.List;

public interface S3CommandService {

    String uploadImage(S3ImageUploadCommand command);

    List<String> uploadImages(S3ImagesUploadCommand command);

    void deleteImage(S3SingleImageDeleteCommand command);

    void deleteImages(S3ImageDeleteCommand command);

    /**
     * S3 내의 한 객체(이미지)를 다른 경로로 복사합니다. (S3-to-S3 copy)
     *
     * @param command 복사 요청 커맨드
     * @return 복사된 이미지의 파일명
     */
    String cloneImageFromUrl(S3ImageCloneCommand command);

    /**
     * S3 내의 여러 객체(이미지)를 다른 경로로 복사합니다.
     *
     * @param command 복사 요청 커맨드
     * @return 복사된 이미지의 파일명 목록
     */
    List<String> cloneImagesFromUrls(S3ImagesCloneCommand command);
}
