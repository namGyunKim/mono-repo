package com.example.global.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    @Test
    void normalizePage_null_returns_default() {
        final int result = PaginationUtils.normalizePage(null);
        assertThat(result).isEqualTo(PaginationUtils.DEFAULT_PAGE);
    }

    @Test
    void normalizePage_zero_returns_default() {
        final int result = PaginationUtils.normalizePage(0);
        assertThat(result).isEqualTo(PaginationUtils.DEFAULT_PAGE);
    }

    @Test
    void normalizePage_negative_returns_default() {
        final int result = PaginationUtils.normalizePage(-5);
        assertThat(result).isEqualTo(PaginationUtils.DEFAULT_PAGE);
    }

    @Test
    void normalizePage_valid_passes_through() {
        final int result = PaginationUtils.normalizePage(3);
        assertThat(result).isEqualTo(3);
    }

    @Test
    void normalizeSize_null_returns_defaultSize() {
        final int result = PaginationUtils.normalizeSize(null, 15);
        assertThat(result).isEqualTo(15);
    }

    @Test
    void normalizeSize_zero_returns_defaultSize() {
        final int result = PaginationUtils.normalizeSize(0, 15);
        assertThat(result).isEqualTo(15);
    }

    @Test
    void normalizeSize_negative_returns_defaultSize() {
        final int result = PaginationUtils.normalizeSize(-1, 15);
        assertThat(result).isEqualTo(15);
    }

    @Test
    void normalizeSize_valid_passes_through() {
        final int result = PaginationUtils.normalizeSize(20, 15);
        assertThat(result).isEqualTo(20);
    }

    @Test
    void normalizeSize_exceeds_maxSize_gets_capped() {
        final int result = PaginationUtils.normalizeSize(500, 10, 100);
        assertThat(result).isEqualTo(100);
    }

    @Test
    void normalizeSize_invalid_maxSize_returns_defaultSize() {
        final int result = PaginationUtils.normalizeSize(50, 10, 0);
        assertThat(result).isEqualTo(10);
    }

    @Test
    void toPageable_converts_1based_page_to_0based() {
        final Pageable pageable = PaginationUtils.toPageable(1, 10, 10);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }

    @Test
    void toPageable_page3_produces_offset_20() {
        final Pageable pageable = PaginationUtils.toPageable(3, 10, 10);
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getOffset()).isEqualTo(20);
    }

    @Test
    void toPageable_null_page_and_size_uses_defaults() {
        final Pageable pageable = PaginationUtils.toPageable(null, null, 15);
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(15);
    }

    @Test
    void toPageable_with_sort_includes_sort_parameter() {
        final Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        final Pageable pageable = PaginationUtils.toPageable(1, 10, 10, sort);
        assertThat(pageable.getSort()).isEqualTo(sort);
    }

    @Test
    void toPageable_with_maxSize_respects_cap() {
        final Pageable pageable = PaginationUtils.toPageable(1, 500, 10, 50);
        assertThat(pageable.getPageSize()).isEqualTo(50);
    }
}
