package com.example.domain.aws.service.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * 임시 파일 생명주기 관리를 담당하는 단일 책임 클래스
 *
 * <p>
 * [책임]
 * - 임시 파일 생성
 * - 스트림에서 임시 파일로 복사
 * - 임시 파일 정리(삭제)
 * </p>
 */
@Slf4j
@Component
public class TempFileHandler {

    private static final String TEMP_FILE_PREFIX = "upload_";
    private static final String TEMP_FILE_SUFFIX_SEPARATOR = "_";

    /**
     * 원본 파일명을 기반으로 임시 파일을 생성합니다.
     *
     * @param originalFilename 원본 파일명
     * @return 생성된 임시 파일
     * @throws IOException 임시 파일 생성 실패 시
     */
    public File createTempFile(final String originalFilename) throws IOException {
        return File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_SEPARATOR + originalFilename);
    }

    /**
     * InputStream의 내용을 임시 파일로 복사합니다.
     *
     * @param inputStream 복사할 스트림
     * @param tempFile    대상 임시 파일
     * @throws IOException 복사 실패 시
     */
    public void copyToTempFile(final InputStream inputStream, final File tempFile) throws IOException {
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * 임시 파일을 안전하게 삭제합니다.
     * 파일이 null이거나 존재하지 않으면 무시합니다.
     * 삭제 실패 시 경고 로그를 남기고 예외를 발생시키지 않습니다.
     *
     * @param tempFile 삭제할 임시 파일 (nullable)
     */
    public void cleanup(final File tempFile) {
        if (tempFile == null || !tempFile.exists()) {
            return;
        }
        if (!tempFile.delete()) {
            log.warn(
                    """
                            임시 파일 삭제 실패: tempFilePath={}
                            """.stripTrailing(),
                    tempFile.getAbsolutePath()
            );
        }
    }

    /**
     * 임시 파일의 절대 경로를 반환합니다.
     * 파일이 null이면 "UNKNOWN"을 반환합니다.
     *
     * @param tempFile 임시 파일 (nullable)
     * @return 파일 경로 또는 "UNKNOWN"
     */
    public String getPathOrUnknown(final File tempFile) {
        return tempFile != null ? tempFile.getAbsolutePath() : "UNKNOWN";
    }
}
