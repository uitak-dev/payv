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
 *
 * - cron/zone은 application.properties에서 조정 가능
 * - 같은 Job이 이미 실행 중이면 중복 실행을 건너뜀
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

    @Scheduled(
            cron = "${automation.batch.fixed-expense.cron:0 5 0 * * *}",
            zone = "${automation.batch.fixed-expense.zone:}"
    )
    public void scheduleFixedExpensePlanningJob() {
        if (!enabled) {
            return;
        }

        try {
            Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(JOB_NAME);
            if (runningExecutions != null && !runningExecutions.isEmpty()) {
                log.info("Skip {} launch: already running {} execution(s)", JOB_NAME, runningExecutions.size());
                return;
            }

            LocalDate runDate = LocalDate.now(resolveZone());
            JobParameters params = new JobParametersBuilder()
                    .addString("runDate", runDate.toString())
                    .addLong("launchedAt", System.currentTimeMillis())
                    .toJobParameters();

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
