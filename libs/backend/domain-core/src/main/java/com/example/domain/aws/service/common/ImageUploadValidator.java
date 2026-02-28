package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 이미지 업로드 검증을 담당하는 단일 책임 클래스
 *
 * <p>
 * [책임]
 * - 파일 크기 검증
 * - 이미지 차원(width/height) 검증
 * </p>
 */
@Component
public class ImageUploadValidator {

    /**
     * 파일 크기를 검증합니다.
     *
     * @param size      파일 크기 (bytes)
     * @param imageType 이미지 타입 (최대 크기 정보 포함)
     * @throws GlobalException 파일 크기 초과 시
     */
    public void validateFileSize(final long size, final ImageType imageType) {
        if (size > imageType.getMaxSize()) {
            throw new GlobalException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    /**
     * 임시 파일의 크기를 검증합니다.
     *
     * @param tempFile  임시 파일
     * @param imageType 이미지 타입 (최대 크기 정보 포함)
     * @throws GlobalException 파일 크기 초과 시
     */
    public void validateTempFileSize(final File tempFile, final ImageType imageType) {
        validateFileSize(tempFile.length(), imageType);
    }

    /**
     * 이미지 차원(width/height)을 검증합니다.
     *
     * @param tempFile  검증할 이미지 파일
     * @param imageType 이미지 타입 (차원 제약 정보 포함)
     * @throws GlobalException 이미지 파일이 유효하지 않거나 차원이 맞지 않을 시
     */
    public void validateImageDimensions(final File tempFile, final ImageType imageType) {
        if (!requiresDimensionValidation(imageType)) {
            return;
        }
        final BufferedImage image = readImage(tempFile);
        validateDimensionMatch(image, imageType);
    }

    private boolean requiresDimensionValidation(final ImageType imageType) {
        return imageType.getWidth() != null || imageType.getHeight() != null;
    }

    private BufferedImage readImage(final File tempFile) {
        try {
            final BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new GlobalException(ErrorCode.INVALID_IMAGE_FILE);
            }
            return image;
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void validateDimensionMatch(final BufferedImage image, final ImageType imageType) {
        final int width = image.getWidth();
        final int height = image.getHeight();

        final boolean widthMismatch = imageType.getWidth() != null && width != imageType.getWidth();
        final boolean heightMismatch = imageType.getHeight() != null && height != imageType.getHeight();

        if (widthMismatch || heightMismatch) {
            throw new GlobalException(ErrorCode.INVALID_IMAGE_DIMENSIONS);
        }
    }
}
