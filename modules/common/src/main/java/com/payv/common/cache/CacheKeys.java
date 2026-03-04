package com.payv.common.cache;

import java.time.LocalDate;
import java.time.YearMonth;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String reportingMonthlySummaryKey(String ownerUserId, YearMonth month) {
        return "owner=" + safe(ownerUserId) + ":month=" + safeMonth(month);
    }

    public static String reportingHomeDashboardKey(String ownerUserId, YearMonth month) {
        return "owner=" + safe(ownerUserId) + ":month=" + safeMonth(month);
    }

    public static String ledgerRecentFirstPageKey(String ownerUserId,
                                                  LocalDate from,
                                                  LocalDate to,
                                                  int size) {
        return "owner=" + safe(ownerUserId)
                + ":from=" + safeDate(from)
                + ":to=" + safeDate(to)
                + ":size=" + Math.max(size, 1);
    }

    public static String budgetMonthlyStatusKey(String ownerUserId, YearMonth month) {
        return "owner=" + safe(ownerUserId) + ":month=" + safeMonth(month);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String safeDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }

    private static String safeMonth(YearMonth value) {
        return value == null ? YearMonth.now().toString() : value.toString();
    }
}
