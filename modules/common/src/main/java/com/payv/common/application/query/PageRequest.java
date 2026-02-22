package com.payv.common.application.query;

import lombok.Getter;

@Getter
public final class PageRequest {

    private final int page;
    private final int size;
    private final int offset;

    private PageRequest(int page, int size, int offset) {
        this.page = page;
        this.size = size;
        this.offset = offset;
    }

    public static PageRequest of(int page, int size) {
        return of(page, size, 10, 100);
    }

    public static PageRequest of(int page, int size, int minSize, int maxSize) {
        int safePage = Math.max(page, 1);
        int boundedMin = Math.max(minSize, 1);
        int boundedMax = Math.max(maxSize, boundedMin);
        int safeSize = Math.min(Math.max(size, boundedMin), boundedMax);
        int offset = (safePage - 1) * safeSize;
        return new PageRequest(safePage, safeSize, offset);
    }
}
