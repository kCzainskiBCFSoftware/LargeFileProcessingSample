package com.daycode.mapper;

import com.daycode.model.TemperatureRecord;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Spring batch record mapper.
 */
public class RecordMapper implements FieldSetMapper<TemperatureRecord> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public TemperatureRecord mapFieldSet(FieldSet fieldSet) {
        TemperatureRecord record = new TemperatureRecord();
        record.setCity(fieldSet.readString("city"));
        record.setTimestamp(LocalDateTime.parse(fieldSet.readString("timestamp"), formatter));
        record.setTemperature(fieldSet.readDouble("temperature"));
        return record;
    }
}
