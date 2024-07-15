package com.daycode.config;

import com.daycode.mapper.RecordMapper;
import com.daycode.model.BatchProperties;
import com.daycode.model.TemperatureRecord;
import com.daycode.service.TemperatureJobListener;
import com.daycode.service.TemperatureService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.Objects;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Value("file:src/main/resources/example_file.csv")
    private Resource inputResource;

    /**
     * @param batchProperties Properties for spring batch.
     * @return TaskExecutor for Spring Batch
     */
    @Bean
    public TaskExecutor taskExecutor(BatchProperties batchProperties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(batchProperties.getCorePoolSize());
        executor.setMaxPoolSize(batchProperties.getMaxPoolSize());
        executor.setQueueCapacity(batchProperties.getQueueCapacity());
        executor.setThreadNamePrefix(batchProperties.getThreadNamePrefix());
        executor.initialize();
        return executor;
    }

    /**
     * Spring batch temperatures job configuration.
     *
     * @return Temperatures job for spring batch.
     */
    @Bean
    public Job importTemperaturesJob(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                                     ItemReader<TemperatureRecord> reader, ItemProcessor<TemperatureRecord, TemperatureRecord> processor,
                                     ItemWriter<TemperatureRecord> writer, TemperatureJobListener temperatureJobListener,
                                     BatchProperties batchProperties, TaskExecutor taskExecutor) {
        Step step = stepBuilderFactory.get("sumTemperatures")
                .<TemperatureRecord, TemperatureRecord>chunk(batchProperties.getChunkSize())
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
        return jobBuilderFactory.get("importTemperatureJob")
                .incrementer(new RunIdIncrementer())
                .listener(temperatureJobListener)
                .flow(step)
                .end()
                .build();
    }

    /**
     * Spring batch reader for temperatures job.
     *
     * @param filePath FilePath taken from jobParameters
     * @return Reader for Spring batch job.
     * @throws IOException is case issues reading the source File.
     */
    @Bean
    @StepScope
    public FlatFileItemReader<TemperatureRecord> reader(@Value("#{jobParameters[filePath]}") String filePath) throws IOException {
        filePath = Objects.isNull(filePath) ? inputResource.getFile().getAbsolutePath() : filePath;
        return new FlatFileItemReaderBuilder<TemperatureRecord>()
                .name("temperatureItemReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .delimiter(";")
                .names("city", "timestamp", "temperature")
                .fieldSetMapper(new RecordMapper())
                .build();
    }

    /**
     * Spring batch job processor.
     *
     * @return Same record as provided in input.
     */
    @Bean
    public ItemProcessor<TemperatureRecord, TemperatureRecord> processor() {
        return record -> record; // No processor, just pass through
    }

    /**
     * Spring batch temperatures job writer using Temperature service.
     *
     * @param temperatureService Temperature Service responsible for handling the data.
     * @return Spring batch item writer.
     */
    @Bean
    public ItemWriter<TemperatureRecord> writer(TemperatureService temperatureService) {
        return temperatureService::processTemperatureRecords;
    }
}

