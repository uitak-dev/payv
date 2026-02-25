package com.payv.automation.application.batch.job;

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
 * 고정비 계획 생성 배치 잡 구성.
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

    /**
     * 배치 잡 진입점.
     * RunIdIncrementer를 둬서 같은 파라미터로도 재실행 가능하게 한다.
     */
    @Bean
    public Job fixedExpensePlanningJob(Step fixedExpensePlanningStep) {
        return jobBuilderFactory.get("fixedExpensePlanningJob")
                .incrementer(new RunIdIncrementer())
                .start(fixedExpensePlanningStep)
                .build();
    }

    /**
     * chunk 기반 step.
     * CHUNK_SIZE 단위로 reader -> processor -> writer를 반복한다.
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
     * jobParameters['runDate']를 받아 실행 대상 마스터 목록을 준비한다.
     * runDate가 없거나 잘못된 형식이면 LocalDate.now()를 사용한다.
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
     * 각 마스터에 runDate 컨텍스트를 붙이는 processor.
     */
    @Bean
    @StepScope
    public ItemProcessor<FixedExpenseDefinition, FixedExpenseExecutionPlanItem> fixedExpenseExecutionProcessor(
            @Value("#{jobParameters['runDate']}") String runDate) {
        return new FixedExpenseExecutionItemProcessor(parseRunDate(runDate));
    }

    /**
     * 실행 인스턴스 생성/중복 방지를 실제로 반영하는 writer.
     */
    @Bean
    public ItemWriter<FixedExpenseExecutionPlanItem> fixedExpenseExecutionWriter() {
        return new FixedExpenseExecutionItemWriter(executionRepository);
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
