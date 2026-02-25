package com.payv.automation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 사용자 고정비 "마스터" Aggregate Root.
 *
 * 이 객체는 "어떤 고정비가, 매달 언제 발생해야 하는가"를 보관한다.
 * 실제 처리 결과(성공/실패)는 FixedExpenseExecution에 별도로 남긴다.
 */
@Getter
public final class FixedExpenseDefinition {

    private final FixedExpenseDefinitionId id;
    private final String ownerUserId;

    private String name;
    private long amount;
    private String assetId;
    private String categoryIdLevel1;
    private String categoryIdLevel2;
    private String memo;

    private FixedExpenseCycle cycle;
    private Integer dayOfMonth;
    private boolean endOfMonth;     // 매월 마지막 날 실행 여부(EOM).
    private boolean active;

    @Builder
    private FixedExpenseDefinition(FixedExpenseDefinitionId id,
                                   String ownerUserId,
                                   String name,
                                   long amount,
                                   String assetId,
                                   String categoryIdLevel1,
                                   String categoryIdLevel2,
                                   String memo,
                                   FixedExpenseCycle cycle,
                                   Integer dayOfMonth,
                                   boolean endOfMonth,
                                   boolean active) {
        this.id = requireId(id);
        this.ownerUserId = requireText(ownerUserId, "ownerUserId");
        this.name = requireText(name, "name");
        this.amount = requirePositive(amount, "amount");
        this.assetId = requireText(assetId, "assetId");
        this.categoryIdLevel1 = requireText(categoryIdLevel1, "categoryIdLevel1");
        this.categoryIdLevel2 = normalizeNullable(categoryIdLevel2);
        this.memo = normalizeNullable(memo);
        this.cycle = cycle == null ? FixedExpenseCycle.MONTHLY : cycle;
        this.dayOfMonth = normalizeDayOfMonth(dayOfMonth, endOfMonth);
        this.endOfMonth = endOfMonth;
        this.active = active;
    }

    public static FixedExpenseDefinition create(String ownerUserId,
                                                String name,
                                                long amount,
                                                String assetId,
                                                String categoryIdLevel1,
                                                String categoryIdLevel2,
                                                String memo,
                                                Integer dayOfMonth,
                                                boolean endOfMonth) {
        return FixedExpenseDefinition.builder()
                .id(FixedExpenseDefinitionId.generate())
                .ownerUserId(ownerUserId)
                .name(name)
                .amount(amount)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .categoryIdLevel2(categoryIdLevel2)
                .memo(memo)
                .cycle(FixedExpenseCycle.MONTHLY)
                .dayOfMonth(dayOfMonth)
                .endOfMonth(endOfMonth)
                .active(true)
                .build();
    }

    public static FixedExpenseDefinition of(FixedExpenseDefinitionId id,
                                            String ownerUserId,
                                            String name,
                                            long amount,
                                            String assetId,
                                            String categoryIdLevel1,
                                            String categoryIdLevel2,
                                            String memo,
                                            FixedExpenseCycle cycle,
                                            Integer dayOfMonth,
                                            boolean endOfMonth,
                                            boolean active) {
        return FixedExpenseDefinition.builder()
                .id(id)
                .ownerUserId(ownerUserId)
                .name(name)
                .amount(amount)
                .assetId(assetId)
                .categoryIdLevel1(categoryIdLevel1)
                .categoryIdLevel2(categoryIdLevel2)
                .memo(memo)
                .cycle(cycle)
                .dayOfMonth(dayOfMonth)
                .endOfMonth(endOfMonth)
                .active(active)
                .build();
    }

    /**
     * runDate가 이 definition의 실행일인지 판단한다.
     * - endOfMonth=true : 해당 월의 마지막 날만 실행 대상
     * - endOfMonth=false: 설정한 dayOfMonth(단, 월 길이를 넘으면 말일 보정)와 일치할 때 실행 대상
     */
    public boolean isScheduledOn(LocalDate runDate) {
        if (!active || runDate == null) {
            return false;
        }

        YearMonth yearMonth = YearMonth.from(runDate);
        int targetDay = resolveScheduledDayOfMonth(yearMonth, dayOfMonth, endOfMonth);
        return runDate.getDayOfMonth() == targetDay;
    }

    public FixedExpenseExecution planExecution(LocalDate scheduledDate) {
        if (!isScheduledOn(scheduledDate)) {
            throw new IllegalStateException("fixed expense is not scheduled on this date");
        }
        return FixedExpenseExecution.plan(this, scheduledDate);
    }

    public void update(String name,
                       long amount,
                       String assetId,
                       String categoryIdLevel1,
                       String categoryIdLevel2,
                       String memo,
                       Integer dayOfMonth,
                       boolean endOfMonth) {
        requireActive();
        this.name = requireText(name, "name");
        this.amount = requirePositive(amount, "amount");
        this.assetId = requireText(assetId, "assetId");
        this.categoryIdLevel1 = requireText(categoryIdLevel1, "categoryIdLevel1");
        this.categoryIdLevel2 = normalizeNullable(categoryIdLevel2);
        this.memo = normalizeNullable(memo);
        this.dayOfMonth = normalizeDayOfMonth(dayOfMonth, endOfMonth);
        this.endOfMonth = endOfMonth;
    }

    public void deactivate() {
        this.active = false;
    }

    private void requireActive() {
        if (!active) {
            throw new IllegalStateException("inactive fixed expense");
        }
    }

    private static FixedExpenseDefinitionId requireId(FixedExpenseDefinitionId value) {
        if (value == null) {
            throw new IllegalArgumentException("fixedExpenseDefinitionId must not be null");
        }
        return value;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static long requirePositive(long value, String fieldName) {
        if (value <= 0L) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static Integer normalizeDayOfMonth(Integer value, boolean endOfMonth) {
        if (endOfMonth) {
            return null;
        }
        if (value == null || value < 1 || value > 31) {
            throw new IllegalArgumentException("dayOfMonth must be between 1 and 31 when endOfMonth=false");
        }
        return value;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static int resolveScheduledDayOfMonth(YearMonth yearMonth, Integer dayOfMonth, boolean endOfMonth) {
        if (endOfMonth) {
            return yearMonth.lengthOfMonth();
        }
        return Math.min(dayOfMonth, yearMonth.lengthOfMonth());
    }
}
