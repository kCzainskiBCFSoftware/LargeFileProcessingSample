package com.daycode.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Record representation from source file.
 */
@Setter
@Getter
public class TemperatureRecord {

    private String city;
    private LocalDateTime timestamp;
    private double temperature;

}

