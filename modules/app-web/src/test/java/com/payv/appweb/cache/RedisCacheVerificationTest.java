package com.payv.appweb.cache;

import com.payv.appweb.config.RedisCacheConfig;
import com.payv.budget.application.port.ClassificationQueryPort;
import com.payv.budget.application.port.LedgerSpendingQueryPort;
import com.payv.budget.application.query.BudgetQueryService;
import com.payv.budget.domain.model.Budget;
import com.payv.budget.domain.model.BudgetId;
import com.payv.budget.domain.repository.BudgetRepository;
import com.payv.common.application.query.PagedResult;
import com.payv.common.cache.CacheNames;
import com.payv.contracts.classification.dto.CategoryTreePublicDto;
import com.payv.contracts.common.dto.IdNamePublicDto;
import com.payv.ledger.application.port.AssetQueryPort;
import com.payv.ledger.application.query.TransactionQueryService;
import com.payv.ledger.infrastructure.persistence.mybatis.mapper.TransactionMapper;
import com.payv.ledger.infrastructure.persistence.mybatis.record.TransactionRecord;
import com.payv.ledger.presentation.dto.viewmodel.TransactionSummaryView;
import com.payv.reporting.application.port.BudgetSnapshotPort;
import com.payv.reporting.application.port.ClassificationLookupPort;
import com.payv.reporting.application.port.LedgerReportQueryPort;
import com.payv.reporting.application.port.dto.AmountByIdDto;
import com.payv.reporting.application.port.dto.OverallBudgetSnapshotDto;
import com.payv.reporting.application.port.dto.RecentTransactionDto;
import com.payv.reporting.application.query.ReportingQueryService;
import com.payv.reporting.application.query.model.HomeDashboardView;
import com.payv.reporting.application.query.model.MonthlyReportView;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisCacheVerificationTest {

    private static final String OWNER = "owner-1";

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        flushDb(context.getBean(RedisConnectionFactory.class));
    }

    @After
    public void tearDown() {
        if (context != null) {
            flushDb(context.getBean(RedisConnectionFactory.class));
            context.close();
        }
    }

    @Test
    public void verifyRedisCachingForReportingLedgerAndBudget() {
        ReportingQueryService reportingQueryService = context.getBean(ReportingQueryService.class);
        TransactionQueryService transactionQueryService = context.getBean(TransactionQueryService.class);
        BudgetQueryService budgetQueryService = context.getBean(BudgetQueryService.class);

        Assert.assertTrue(AopUtils.isAopProxy(reportingQueryService));
        Assert.assertTrue(AopUtils.isAopProxy(transactionQueryService));
        Assert.assertTrue(AopUtils.isAopProxy(budgetQueryService));

        CountingLedgerReportQueryPort reportingLedgerPort = context.getBean(CountingLedgerReportQueryPort.class);
        CountingTransactionMapper countingTransactionMapper = context.getBean(CountingTransactionMapper.class);
        CountingBudgetRepository countingBudgetRepository = context.getBean(CountingBudgetRepository.class);

        YearMonth month = YearMonth.of(2026, 3);
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        MonthlyReportView monthly1 = reportingQueryService.getMonthlyReport(OWNER, month);
        MonthlyReportView monthly2 = reportingQueryService.getMonthlyReport(OWNER, month);
        Assert.assertEquals(monthly1.getTotalExpense(), monthly2.getTotalExpense());

        HomeDashboardView home1 = reportingQueryService.getHomeDashboard(OWNER, month);
        HomeDashboardView home2 = reportingQueryService.getHomeDashboard(OWNER, month);
        Assert.assertEquals(home1.getExpenseAmount(), home2.getExpenseAmount());

        PagedResult<TransactionSummaryView> recent1 = transactionQueryService.list(OWNER, from, to, null, 1, 20);
        PagedResult<TransactionSummaryView> recent2 = transactionQueryService.list(OWNER, from, to, null, 1, 20);
        Assert.assertEquals(recent1.getTotal(), recent2.getTotal());

        budgetQueryService.getMonthlyBudgets(OWNER, month);
        budgetQueryService.getMonthlyBudgets(OWNER, month);

        Assert.assertEquals(4, reportingLedgerPort.sumAmountByTypeCalls.get());
        Assert.assertEquals(1, reportingLedgerPort.sumExpenseByAssetCalls.get());
        Assert.assertEquals(1, reportingLedgerPort.sumExpenseByCategoryCalls.get());
        Assert.assertEquals(1, reportingLedgerPort.sumExpenseByTagCalls.get());
        Assert.assertEquals(1, reportingLedgerPort.findRecentTransactionsCalls.get());

        Assert.assertEquals(1, countingTransactionMapper.selectListCalls.get());
        Assert.assertEquals(1, countingTransactionMapper.countListCalls.get());

        Assert.assertEquals(1, countingBudgetRepository.findAllByOwnerAndMonthCalls.get());

        assertCacheEntryExists(CacheNames.REPORTING_MONTHLY_SUMMARY);
        assertCacheEntryExists(CacheNames.REPORTING_HOME_DASHBOARD);
        assertCacheEntryExists(CacheNames.LEDGER_RECENT_FIRST_PAGE);
        assertCacheEntryExists(CacheNames.BUDGET_MONTHLY_STATUS);
    }

    private void assertCacheEntryExists(String cacheName) {
        Cache cache = context.getBean("cacheManager", org.springframework.cache.CacheManager.class).getCache(cacheName);
        Assert.assertNotNull(cache);

        RedisConnectionFactory connectionFactory = context.getBean(RedisConnectionFactory.class);
        try (RedisConnection connection = connectionFactory.getConnection()) {
            Set<byte[]> keys = connection.keys((cacheName + "::*").getBytes());
            Assert.assertNotNull(keys);
            Assert.assertFalse("cache is empty: " + cacheName, keys.isEmpty());
        }
    }

    private static void flushDb(RedisConnectionFactory factory) {
        try (RedisConnection connection = factory.getConnection()) {
            connection.flushDb();
        }
    }

    @Configuration
    @Import(RedisCacheConfig.class)
    static class TestConfig {

        @Bean
        public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
            PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
            Properties props = new Properties();
            props.setProperty("redis.host", "localhost");
            props.setProperty("redis.port", "6379");
            props.setProperty("redis.password", "");
            props.setProperty("redis.database", "15");
            props.setProperty("redis.command-timeout-ms", "2000");
            props.setProperty("cache.ttl.default-seconds", "300");
            props.setProperty("cache.ttl.reporting-monthly-seconds", "600");
            props.setProperty("cache.ttl.reporting-home-seconds", "180");
            props.setProperty("cache.ttl.ledger-recent-seconds", "120");
            props.setProperty("cache.ttl.budget-monthly-seconds", "300");
            configurer.setProperties(props);
            return configurer;
        }

        @Bean
        public CacheErrorHandler cacheErrorHandler() {
            return new CacheErrorHandler() {
                @Override
                public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                    throw exception;
                }

                @Override
                public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                    throw exception;
                }

                @Override
                public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                    throw exception;
                }

                @Override
                public void handleCacheClearError(RuntimeException exception, Cache cache) {
                    throw exception;
                }
            };
        }

        @Bean
        public ReportingQueryService reportingQueryService() {
            return new ReportingQueryService(
                    countingLedgerReportQueryPort(),
                    fixedBudgetSnapshotPort(),
                    fixedAssetLookupPort(),
                    fixedClassificationLookupPort()
            );
        }

        @Bean
        public TransactionQueryService transactionQueryService() {
            return new TransactionQueryService(
                    countingTransactionMapper(),
                    new NoOpAttachmentRepository(),
                    fixedLedgerClassificationQueryPort(),
                    fixedLedgerAssetQueryPort()
            );
        }

        @Bean
        public BudgetQueryService budgetQueryService() {
            return new BudgetQueryService(
                    countingBudgetRepository(),
                    fixedBudgetClassificationQueryPort(),
                    fixedLedgerSpendingQueryPort()
            );
        }

        @Bean
        public CountingLedgerReportQueryPort countingLedgerReportQueryPort() {
            return new CountingLedgerReportQueryPort();
        }

        @Bean
        public CountingTransactionMapper countingTransactionMapper() {
            return new CountingTransactionMapper();
        }

        @Bean
        public CountingBudgetRepository countingBudgetRepository() {
            return new CountingBudgetRepository();
        }

        @Bean
        public BudgetSnapshotPort fixedBudgetSnapshotPort() {
            return (ownerUserId, targetMonth) -> Optional.of(new OverallBudgetSnapshotDto("b-overall", 100000L, 10000L, 90000L, 10));
        }

        @Bean
        public com.payv.reporting.application.port.AssetLookupPort fixedAssetLookupPort() {
            return (assetIds, ownerUserId) -> toRows(assetIds, "asset-");
        }

        @Bean
        public ClassificationLookupPort fixedClassificationLookupPort() {
            return new ClassificationLookupPort() {
                @Override
                public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
                    return toRows(categoryIds, "cat-");
                }

                @Override
                public List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId) {
                    return toRows(tagIds, "tag-");
                }
            };
        }

        @Bean
        public com.payv.ledger.application.port.ClassificationQueryPort fixedLedgerClassificationQueryPort() {
            return new com.payv.ledger.application.port.ClassificationQueryPort() {
                @Override
                public List<IdNamePublicDto> getTagNames(Collection<String> tagIds, String ownerUserId) {
                    return Collections.emptyList();
                }

                @Override
                public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
                    return toRows(categoryIds, "cat-");
                }

                @Override
                public List<IdNamePublicDto> getAllTags(String ownerUserId) {
                    return Collections.emptyList();
                }

                @Override
                public List<CategoryTreePublicDto> getAllCategories(String ownerUserId) {
                    return Collections.emptyList();
                }
            };
        }

        @Bean
        public AssetQueryPort fixedLedgerAssetQueryPort() {
            return new AssetQueryPort() {
                @Override
                public List<IdNamePublicDto> getAssetNames(Collection<String> assetIds, String ownerUserId) {
                    return toRows(assetIds, "asset-");
                }

                @Override
                public List<IdNamePublicDto> getAllActiveAssets(String ownerUserId) {
                    return Collections.emptyList();
                }
            };
        }

        @Bean
        public ClassificationQueryPort fixedBudgetClassificationQueryPort() {
            return new ClassificationQueryPort() {
                @Override
                public List<IdNamePublicDto> getCategoryNames(Collection<String> categoryIds, String ownerUserId) {
                    return toRows(categoryIds, "cat-");
                }

                @Override
                public List<CategoryTreePublicDto> getAllCategories(String ownerUserId) {
                    return Collections.emptyList();
                }
            };
        }

        @Bean
        public LedgerSpendingQueryPort fixedLedgerSpendingQueryPort() {
            return (ownerUserId, from, to, categoryIdLevel1) -> 10000L;
        }

        private List<IdNamePublicDto> toRows(Collection<String> ids, String prefix) {
            if (ids == null || ids.isEmpty()) {
                return Collections.emptyList();
            }
            List<IdNamePublicDto> result = new ArrayList<>();
            for (String id : ids) {
                result.add(new IdNamePublicDto(id, prefix + id));
            }
            return result;
        }
    }

    static class CountingLedgerReportQueryPort implements LedgerReportQueryPort {
        private final AtomicInteger sumAmountByTypeCalls = new AtomicInteger();
        private final AtomicInteger sumExpenseByAssetCalls = new AtomicInteger();
        private final AtomicInteger sumExpenseByCategoryCalls = new AtomicInteger();
        private final AtomicInteger sumExpenseByTagCalls = new AtomicInteger();
        private final AtomicInteger findRecentTransactionsCalls = new AtomicInteger();

        @Override
        public long sumAmountByType(String ownerUserId, LocalDate from, LocalDate to, String transactionType) {
            sumAmountByTypeCalls.incrementAndGet();
            return "EXPENSE".equals(transactionType) ? 30000L : 10000L;
        }

        @Override
        public List<AmountByIdDto> sumExpenseByAsset(String ownerUserId, LocalDate from, LocalDate to) {
            sumExpenseByAssetCalls.incrementAndGet();
            return Collections.singletonList(new AmountByIdDto("a-1", 30000L));
        }

        @Override
        public List<AmountByIdDto> sumExpenseByCategoryLevel1(String ownerUserId, LocalDate from, LocalDate to) {
            sumExpenseByCategoryCalls.incrementAndGet();
            return Collections.singletonList(new AmountByIdDto("c-1", 30000L));
        }

        @Override
        public List<AmountByIdDto> sumExpenseByTag(String ownerUserId, LocalDate from, LocalDate to) {
            sumExpenseByTagCalls.incrementAndGet();
            return Collections.singletonList(new AmountByIdDto("t-1", 15000L));
        }

        @Override
        public List<RecentTransactionDto> findRecentTransactions(String ownerUserId, LocalDate from, LocalDate to, int limit) {
            findRecentTransactionsCalls.incrementAndGet();
            return Collections.singletonList(new RecentTransactionDto("tx-1", "EXPENSE", 15000L, LocalDate.of(2026, 3, 1), "a-1", "c-1", "memo"));
        }
    }

    static class CountingBudgetRepository implements BudgetRepository {

        private final AtomicInteger findAllByOwnerAndMonthCalls = new AtomicInteger();

        @Override
        public void save(Budget budget, String ownerUserId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Budget> findById(BudgetId budgetId, String ownerUserId) {
            return Optional.empty();
        }

        @Override
        public List<Budget> findAllByOwnerAndMonth(String ownerUserId, YearMonth targetMonth) {
            findAllByOwnerAndMonthCalls.incrementAndGet();
            return Collections.singletonList(Budget.create(ownerUserId, targetMonth, 100000L, null, "overall"));
        }

        @Override
        public int deactivateOrphanedCategoryBudgets(String ownerUserId, Set<String> activeRootCategoryIds) {
            return 0;
        }
    }

    static class CountingTransactionMapper implements TransactionMapper {

        private final AtomicInteger selectListCalls = new AtomicInteger();
        private final AtomicInteger countListCalls = new AtomicInteger();

        @Override
        public List<TransactionRecord> selectList(String ownerUserId,
                                                  LocalDate from,
                                                  LocalDate to,
                                                  String assetId,
                                                  int offset,
                                                  int limit) {
            selectListCalls.incrementAndGet();
            return Collections.singletonList(TransactionRecord.builder()
                    .transactionId("tx-1")
                    .ownerUserId(ownerUserId)
                    .transactionType("EXPENSE")
                    .amount(20000L)
                    .transactionDate(LocalDate.of(2026, 3, 1))
                    .assetId("a-1")
                    .memo("memo")
                    .categoryIdLevel1("c-1")
                    .sourceType("MANUAL")
                    .build());
        }

        @Override
        public int countList(String ownerUserId, LocalDate from, LocalDate to, String assetId) {
            countListCalls.incrementAndGet();
            return 1;
        }

        @Override
        public TransactionRecord selectDetail(String transactionId, String ownerUserId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<String> selectTagIds(String transactionId, String ownerUserId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long sumExpenseAmount(String ownerUserId, LocalDate from, LocalDate to, String categoryIdLevel1, String categoryIdLevel2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long sumAmountByType(String ownerUserId, LocalDate from, LocalDate to, String transactionType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int upsert(TransactionRecord record) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int deleteByIdAndOwner(String transactionId, String ownerUserId) {
            throw new UnsupportedOperationException();
        }
    }

    static class NoOpAttachmentRepository implements com.payv.ledger.domain.repository.AttachmentRepository {

        @Override
        public int countActiveByTransactionId(com.payv.ledger.domain.model.TransactionId id, String ownerUserId) {
            return 0;
        }

        @Override
        public List<com.payv.ledger.domain.model.Attachment> findStoredByTransactionId(com.payv.ledger.domain.model.TransactionId id, String ownerUserId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<com.payv.ledger.domain.model.Attachment> findById(com.payv.ledger.domain.model.AttachmentId id, String ownerUserId) {
            return Optional.empty();
        }

        @Override
        public void insertUploading(com.payv.ledger.domain.model.Attachment attachment) {
        }

        @Override
        public void deleteById(com.payv.ledger.domain.model.AttachmentId id, String ownerUserId) {
        }

        @Override
        public void markStored(com.payv.ledger.domain.model.AttachmentId id, String ownerUserId) {
        }

        @Override
        public void markFailed(com.payv.ledger.domain.model.AttachmentId id, String ownerUserId, String failureReason) {
        }
    }
}
