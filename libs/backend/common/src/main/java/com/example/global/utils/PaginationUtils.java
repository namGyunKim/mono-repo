package com.example.global.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이징(page/size) 기본값 및 방어 로직을 한 곳에서 관리하기 위한 유틸리티입니다.
 *
 * <p>
 * - page는 1부터 시작(외부 입력)
 * - Pageable(PageRequest)는 0부터 시작(스프링 데이터)
 * - size는 과도한 조회를 방지하기 위해 maxSize 상한을 둡니다.
 * </p>
 */
public final class PaginationUtils {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int DEFAULT_LOG_SIZE = 20;

    /**
     * 기본 maxSize 정책
     * - 베이스 프로젝트의 안전장치 목적(과도한 size로 인한 부하/메모리 사용 방지)
     */
    public static final int DEFAULT_MAX_SIZE = 200;

    private PaginationUtils() {
    }

    public static int normalizePage(final Integer page) {
        if (page == null || page < 1) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int normalizeSize(final Integer size, final int defaultSize) {
        return normalizeSize(size, defaultSize, DEFAULT_MAX_SIZE);
    }

    public static int normalizeSize(final Integer size, final int defaultSize, final int maxSize) {
        if (size == null || size < 1) {
            return defaultSize;
        }
        if (maxSize < 1) {
            // 방어: maxSize가 잘못 설정된 경우 defaultSize를 사용
            return defaultSize;
        }
        return Math.min(size, maxSize);
    }

    public static Pageable toPageable(final Integer page, final Integer size, final int defaultSize, final Sort sort) {
        return toPageable(page, size, defaultSize, DEFAULT_MAX_SIZE, sort);
    }

    public static Pageable toPageable(final Integer page, final Integer size, final int defaultSize, final int maxSize, final Sort sort) {
        final int normalizedPage = normalizePage(page);
        final int normalizedSize = normalizeSize(size, defaultSize, maxSize);
        return PageRequest.of(normalizedPage - 1, normalizedSize, sort);
    }

    public static Pageable toPageable(final Integer page, final Integer size, final int defaultSize) {
        return toPageable(page, size, defaultSize, DEFAULT_MAX_SIZE);
    }

    public static Pageable toPageable(final Integer page, final Integer size, final int defaultSize, final int maxSize) {
        final int normalizedPage = normalizePage(page);
        final int normalizedSize = normalizeSize(size, defaultSize, maxSize);
        return PageRequest.of(normalizedPage - 1, normalizedSize);
    }
}
