package com.daycode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Average temperatures data representation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearlyAverageTemperature {
    private String year;
    private double averageTemperature;
}

