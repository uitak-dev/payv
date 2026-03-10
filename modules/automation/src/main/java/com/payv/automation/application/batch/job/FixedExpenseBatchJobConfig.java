package com.payv.automation.application.batch.job;

import com.payv.automation.application.port.LedgerTransactionPort;
import com.payv.automation.domain.model.FixedExpenseDefinition;
import com.payv.automation.domain.repository.FixedExpenseDefinitionRepository;
import com.payv.automation.domain.repository.FixedExpenseExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 고정비 계획 생성 배치 Job(배치 작업의 단위) 구성.
 *
 * 흐름:
 * 1) Reader: 실행 대상 마스터 목록 조회
 * 2) Processor: 마스터 + runDate를 처리 단위 객체로 래핑
 * 3) Writer: runDate가 스케줄과 맞는 정의만 idempotent하게 실행 인스턴스 생성
 */
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class FixedExpenseBatchJobConfig {

    private static final int CHUNK_SIZE = 100;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final FixedExpenseDefinitionRepository definitionRepository;
    private final FixedExpenseExecutionRepository executionRepository;
    private final LedgerTransactionPort ledgerTransactionPort;

    /**
     * 배치 잡 진입점.
     * - RunIdIncrementer를 둬서 같은 파라미터로도 재실행 가능하게 한다.
     */
    @Bean
    public Job fixedExpensePlanningJob(Step fixedExpensePlanningStep) {
        return jobBuilderFactory.get("fixedExpensePlanningJob")
                .incrementer(new RunIdIncrementer())
                .start(fixedExpensePlanningStep)
                .build();
    }

    /**
     * chunk 기반 Step.
     * CHUNK_SIZE 단위로, Reader -> Processor -> Writer를 반복한다.
     */
    @Bean
    public Step fixedExpensePlanningStep(ItemReader<FixedExpenseDefinition> fixedExpenseDefinitionReader,
                                         ItemProcessor<FixedExpenseDefinition, FixedExpenseExecutionPlanItem> fixedExpenseExecutionProcessor,
                                         ItemWriter<FixedExpenseExecutionPlanItem> fixedExpenseExecutionWriter) {
        return stepBuilderFactory.get("fixedExpensePlanningStep")
                .<FixedExpenseDefinition, FixedExpenseExecutionPlanItem>chunk(CHUNK_SIZE)
                .reader(fixedExpenseDefinitionReader)
                .processor(fixedExpenseExecutionProcessor)
                .writer(fixedExpenseExecutionWriter)
                .build();
    }

    /**
     * [ Reader ]
     * 1. 스케줄러가 넘겨준 `runDate` 파라미터를 읽는다. (`runDate`가 없거나 잘못된 형식이면 `LocalDate.now()`를 사용한다.)
     * 2. `definitionRepository`를 통해 해당 날짜에 실행해야 할 고정 지출 정의(Master Data) 목록을 DB에서 가져온다.
     */
    @Bean
    @StepScope
    public ItemReader<FixedExpenseDefinition> fixedExpenseDefinitionReader(
            @Value("#{jobParameters['runDate']}") String runDate) {
        LocalDate baseDate = parseRunDate(runDate);
        List<FixedExpenseDefinition> scheduledDefinitions = definitionRepository.findAllActiveScheduledOn(baseDate);
        return new FixedExpenseDefinitionReader(scheduledDefinitions);
    }

    /**
     * [ Processor ]
     * 1. 읽어온 고정 지출 정의를 바탕으로, 실제 지출 실행 계획(Execution Plan)으로 변환한다.
     * 2. processor: 각 마스터에 runDate 컨텍스트를 붙인다.
     */
    @Bean
    @StepScope
    public ItemProcessor<FixedExpenseDefinition, FixedExpenseExecutionPlanItem> fixedExpenseExecutionProcessor(
            @Value("#{jobParameters['runDate']}") String runDate) {
        return new FixedExpenseExecutionItemProcessor(parseRunDate(runDate));
    }

    /**
     * [ Writer ]
     * 1. 최종 변환된 계획들을 CHUNK_SIZE(100개)씩 묶어서, 실제 거래를 생성한다.
     */
    @Bean
    public ItemWriter<FixedExpenseExecutionPlanItem> fixedExpenseExecutionWriter() {
        return new FixedExpenseExecutionItemWriter(executionRepository, ledgerTransactionPort);
    }

    private LocalDate parseRunDate(String runDate) {
        if (runDate == null || runDate.trim().isEmpty()) {
            return LocalDate.now();
        }
        try {
            // 기대 포맷: yyyy-MM-dd
            return LocalDate.parse(runDate.trim());
        } catch (DateTimeParseException e) {
            // 안전한 fallback: today
            return LocalDate.now();
        }
    }
}
