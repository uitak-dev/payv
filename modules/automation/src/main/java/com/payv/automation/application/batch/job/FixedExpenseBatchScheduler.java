package com.payv.automation.application.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

/**
 * 고정비 계획 배치를 자동 실행하는 스케줄러.
 * - cron/zone은 application.properties에서 조정.
 * - 같은 Job이 이미 실행 중이면 중복 실행을 건너뜀.
 * - runDate(비즈니스 기준일) + launchedAt(잡 인스턴스 구분값) 파라미터 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FixedExpenseBatchScheduler {

    private static final String JOB_NAME = "fixedExpensePlanningJob";

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    @Qualifier(JOB_NAME)
    private final Job fixedExpensePlanningJob;

    @Value("${automation.batch.fixed-expense.enabled:true}")
    private boolean enabled;

    @Value("${automation.batch.fixed-expense.zone:}")
    private String zone;

    /**
     * 설정된 스케줄(기본값: 매일 00:05)에 따라 고정 지출 계획 배치 작업을 실행.
     */
    @Scheduled(
            cron = "${automation.batch.fixed-expense.cron:0 5 0 * * *}",
            zone = "${automation.batch.fixed-expense.zone:}"
    )
    public void scheduleFixedExpensePlanningJob() {
        // 1) 배치 활성화 여부 확인. (설정 파일의 enabled 값에 따라 실행 여부 결정)
        if (!enabled) {
            return;
        }

        try {
            // 2) 중복 실행 방지: 현재 같은 이름의 Job이 실행 중인지 확인.
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(JOB_NAME);
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                log.info("Skip {} launch: already running {} execution(s)", JOB_NAME, runningExecutions.size());
                return;
            }

            // 3) 실행 파라미터 생성
            // - runDate: 배치 기준 날짜
            // - launchedAt: 동일 파라미터로 인한 재실행 방지 및 고유 인스턴스 생성을 위한 타임스탬프
            LocalDate runDate = LocalDate.now(resolveZone());
            JobParameters params = new JobParametersBuilder()
                    .addString("runDate", runDate.toString())
                    .addLong("launchedAt", System.currentTimeMillis())
                    .toJobParameters();

            // 4) Batch Job 실행
            JobExecution execution = jobLauncher.run(fixedExpensePlanningJob, params);

            log.info("Launched {} with executionId={}, runDate={}, status={}",
                    JOB_NAME,
                    execution.getId(),
                    runDate,
                    execution.getStatus());
        } catch (Exception e) {
            log.error("Failed to launch {}", JOB_NAME, e);
        }
    }

    private ZoneId resolveZone() {
        if (zone == null || zone.trim().isEmpty()) {
            return ZoneId.systemDefault();
        }
        return ZoneId.of(zone.trim());
    }
}
