package com.daycode.controller;

import com.daycode.service.TemperatureService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(TemperatureController.class)
public class TemperatureControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TemperatureService temperatureService;

    @MockBean
    private Job importTemperaturesJob;

    @MockBean
    private JobLauncher jobLauncher;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void averageTemperatures_validCity_isOk() {
        String city = "SampleCity";
        Map<Integer, Double> yearlyAverages = new HashMap<>();
        yearlyAverages.put(2021, 15.0);
        yearlyAverages.put(2022, 14.5);

        when(temperatureService.getYearlyAverageTemperatures(city.toLowerCase().trim())).thenReturn(yearlyAverages);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/average-temperatures").queryParam("city", city).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].year").isEqualTo("2021")
                .jsonPath("$[0].averageTemperature").isEqualTo(15.0)
                .jsonPath("$[1].year").isEqualTo("2022")
                .jsonPath("$[1].averageTemperature").isEqualTo(14.5);
    }

    @Test
    void averageTemperatures_invalidCity_notFound() {
        String city = "UnknownCity";

        when(temperatureService.getYearlyAverageTemperatures(city.toLowerCase().trim())).thenReturn(new HashMap<>());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/average-temperatures").queryParam("city", city).build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void averageTemperatures_emptyString_notFound() {
        String city = "";

        when(temperatureService.getYearlyAverageTemperatures(city.toLowerCase().trim())).thenReturn(new HashMap<>());

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/average-temperatures").queryParam("city", city).build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void averageTemperatures_emptyString_notFoundWithNull() {
        String city = "";

        when(temperatureService.getYearlyAverageTemperatures(city.toLowerCase().trim())).thenReturn(null);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/average-temperatures").queryParam("city", city).build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @SneakyThrows
    @Test
    void updateData_validData_isOk() {
        when(jobLauncher.run(any(), any())).thenReturn(new JobExecution(12345L));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/update-data").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody();

        verify(temperatureService, times(1)).clearData();
    }

    @SneakyThrows
    @Test
    void updateData_jobException_is500() {
        doThrow(new JobParametersInvalidException("test")).when(jobLauncher).run(any(), any());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/update-data").build())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody();

        verify(temperatureService, times(1)).clearData();
    }
}