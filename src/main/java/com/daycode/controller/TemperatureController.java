package com.daycode.controller;

import com.daycode.model.YearlyAverageTemperature;
import com.daycode.service.TemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureService temperatureService;
    private final Job importTemperaturesJob;
    private final JobLauncher jobLauncher;

    private final ReentrantLock lock = new ReentrantLock();

    @Value("file:src/main/resources/large_file.csv")
    private Resource inputResource;

    /**
     * Retrieve average temperatures for given city.
     *
     * @param city String with city name.
     * @return Averages for given city name calculated from file.
     */
    @GetMapping("/average-temperatures")
    public Mono<ResponseEntity<?>> getAverageTemperatures(@RequestParam String city) {
        var trimedCity = city.toLowerCase().trim();
        return Mono.fromSupplier(() -> temperatureService.getYearlyAverageTemperatures(trimedCity))
                .map(averages -> {
                    if (averages == null || averages.isEmpty()) {
                        return ResponseEntity.notFound().build();
                    }

                    List<YearlyAverageTemperature> response = averages.entrySet().stream()
                            .map(entry -> new YearlyAverageTemperature(entry.getKey().toString(), Math.round(entry.getValue() * 10.0) / 10.0))
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(response);
                })
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Trigger file reload job.
     *
     * @return JobExecution summary.
     */
    @PostMapping("/update-data")
    public Mono<ResponseEntity<JobInstance>> updateData() {

        return Mono.fromCallable(() -> {
                    lock.lock();
                    try {
                        return triggerJob();
                    } finally {
                        lock.unlock();
                    }
                })
                .map(jobExecution -> ResponseEntity.ok(jobExecution.getJobInstance()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    private JobExecution triggerJob() {
        try {
            Path path = inputResource.getFile().toPath().getParent();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .addString("filePath", path.toAbsolutePath() + path.getFileSystem().getSeparator() + "large_file.csv")
                    .toJobParameters();

            temperatureService.clearData();
            return jobLauncher.run(importTemperaturesJob, jobParameters);
        } catch (Exception e) {
            log.error("Failed to Trigger importTemperaturesJob.", e);
            throw new RuntimeException(e); //TODO: error handling
        }
    }

}

