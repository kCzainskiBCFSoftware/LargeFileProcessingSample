package com.daycode.service;

import com.daycode.model.TemperatureRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemperatureServiceTest {

    private TemperatureService temperatureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        temperatureService = new TemperatureService();
    }

    @Test
    void testGetYearlyAverageTemperatures_validInput_validOutput() {
        String city = "SampleCity";
        initServiceWithSampleData(city);

        var result = temperatureService.getYearlyAverageTemperatures(city);

        assertEquals(15.0, result.get(2021));
    }

    @Test
    void testGetYearlyAverageTemperatures_unknownCity_CityNotFound() {
        String city = "UnknownCity";

        var result = temperatureService.getYearlyAverageTemperatures(city);

        assertTrue(result.isEmpty());
    }

    @Test
    void testProcessTemperatureRecords_validInput_validOutput() {
        TemperatureRecord record1 = new TemperatureRecord();
        String sampleCity = "SampleCity";
        record1.setCity(sampleCity);
        record1.setTimestamp(LocalDateTime.of(2021, 1, 1, 0, 0));
        record1.setTemperature(15.0);

        TemperatureRecord record2 = new TemperatureRecord();
        record2.setCity(sampleCity);
        record2.setTimestamp(LocalDateTime.of(2021, 2, 1, 0, 0));
        record2.setTemperature(15.0);

        temperatureService.processTemperatureRecords(List.of(record1, record2));

        var result = temperatureService.getYearlyAverageTemperatures(sampleCity.toLowerCase().trim());

        assertEquals(15.0, result.get(2021));
    }

    @Test
    void testProcessTemperatureRecords_emptyInput_emptyOutput() {
        String sampleCity = "SampleCity";
        TemperatureRecord record1 = new TemperatureRecord();

        TemperatureRecord record2 = new TemperatureRecord();

        temperatureService.processTemperatureRecords(List.of(record1, record2));

        var result = temperatureService.getYearlyAverageTemperatures(sampleCity.toLowerCase().trim());

        assertTrue(result.isEmpty());
    }

    @Test
    void clearCache() {
        temperatureService.clearCache();
    }

    @Test
    void clearData_validExecution() {
        var sampleCity = "SampleCity";
        initServiceWithSampleData(sampleCity);

        temperatureService.clearData();
        var result = temperatureService.getYearlyAverageTemperatures(sampleCity.toLowerCase().trim());

        assertTrue(result.isEmpty());
    }

    private void initServiceWithSampleData(String city) {
        Map<Integer, Double> yearlyTemps = new HashMap<>();
        yearlyTemps.put(2021, 30.0);
        Map<Integer, Integer> yearlyCounts = new HashMap<>();
        yearlyCounts.put(2021, 2);
        Map<String, Map<Integer, Double>> cityYearlyTemperatures = new HashMap<>();
        cityYearlyTemperatures.put(city, yearlyTemps);
        Map<String, Map<Integer, Integer>> cityYearlyCounts = new HashMap<>();
        cityYearlyCounts.put(city, yearlyCounts);
        temperatureService = new TemperatureService(cityYearlyTemperatures, cityYearlyCounts);
    }
}