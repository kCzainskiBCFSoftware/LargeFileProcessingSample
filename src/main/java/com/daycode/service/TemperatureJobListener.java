package com.daycode.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

/**
 * Spring batch job listener. Configures actions after job is finished.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureJobListener extends JobExecutionListenerSupport {

    private final TemperatureService temperatureService;

    @Override
    public void afterJob(JobExecution jobExecution) {
        super.afterJob(jobExecution);
        if (!jobExecution.getStatus().isUnsuccessful()) {
            temperatureService.clearCache();
        }
    }
}
