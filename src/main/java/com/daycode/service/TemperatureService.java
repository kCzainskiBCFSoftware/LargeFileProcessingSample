package com.daycode.service;

import com.daycode.config.CacheConfig;
import com.daycode.model.TemperatureRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for managing temperatures.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final Map<String, Map<Integer, Double>> cityYearlyTemperatures;
    private final Map<String, Map<Integer, Integer>> cityYearlyCounts;

    /**
     * Default constructor.
     */
    public TemperatureService() {
        cityYearlyTemperatures = new ConcurrentHashMap<>();
        cityYearlyCounts = new ConcurrentHashMap<>();
    }

    /**
     * Process Temperature records from spring batch job.
     *
     * @param records List of records to process
     */
    public synchronized void processTemperatureRecords(List<? extends TemperatureRecord> records) {
        for (TemperatureRecord record : records) {
            if (record.getCity() != null && !record.getCity().isBlank()) {
                String city = record.getCity().toLowerCase().trim();
                LocalDateTime timestamp = record.getTimestamp();
                double temperature = record.getTemperature();

                cityYearlyTemperatures.putIfAbsent(city, new ConcurrentHashMap<>());
                cityYearlyCounts.putIfAbsent(city, new ConcurrentHashMap<>());

                Map<Integer, Double> yearlyTemps = cityYearlyTemperatures.get(city);
                Map<Integer, Integer> yearlyCounts = cityYearlyCounts.get(city);

                yearlyTemps.merge(timestamp.getYear(), temperature, Double::sum);
                yearlyCounts.merge(timestamp.getYear(), 1, Integer::sum);
            }
        }
    }


    /**
     * Calculates averages for given city using precalculated data. Data is cached.
     *
     * @param city String city name for which data should be returned.
     * @return Map with Years and respective averages for given city.
     */
    @Cacheable(value = CacheConfig.TEMPERATURES_CACHE_NAME, key = "#city")
    public Map<Integer, Double> getYearlyAverageTemperatures(String city) {
        synchronized (this) {
            Map<Integer, Double> yearlyTemps = cityYearlyTemperatures.get(city);
            Map<Integer, Integer> yearlyCounts = cityYearlyCounts.get(city);
            log.debug("Found: {} records for city: {}, temps: {}", yearlyCounts, city, yearlyTemps);
            if (yearlyTemps == null || yearlyCounts == null) {
                return new HashMap<>();
            }

            return yearlyTemps.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() / yearlyCounts.get(entry.getKey())
                    ));
        }
    }

    /**
     * Clears Spring cache.
     */
    @CacheEvict(value = CacheConfig.TEMPERATURES_CACHE_NAME, allEntries = true)
    public void clearCache() {
        log.info("Cache clear called!");
    }

    /**
     * Clears precalculated data.
     */
    public void clearData() {
        cityYearlyTemperatures.clear();
        cityYearlyCounts.clear();
    }

}
