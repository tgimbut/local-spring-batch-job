package com.tgimbut.springbatch.configuration;

import com.tgimbut.springbatch.domain.Beer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class BatchJobConfiguration {

    private static final String UTF_8 = "UTF-8";
    private static final String DELIMITER = ";";

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job ratingBeerJob(Step importStep,
                             Step ratingStep,
                             Step exportStep) {
        return jobBuilderFactory.get("ratingBeerJob")
                .flow(importStep)
                .next(ratingStep)
                .next(exportStep)
                .end()
                .build();
    }

    @Bean
    public Step importStep(ItemReader<Beer> importReader,
                           ItemWriter<Beer> importWriter) {
        return stepBuilderFactory.get("importStep")
                .<Beer, Beer>chunk(10)
                .reader(importReader)
                .writer(importWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Beer> importReader(@Value("#{jobParameters[inputFile]}") String pathToFile) {
        FlatFileItemReader<Beer> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(pathToFile));
        reader.setLineMapper(createLineMapper());
        reader.setEncoding(UTF_8);
        return reader;
    }

    private DefaultLineMapper<Beer> createLineMapper() {
        DefaultLineMapper<Beer> defaultLineMapper = new DefaultLineMapper<>();
        defaultLineMapper.setLineTokenizer(createLineTokenizer());
        defaultLineMapper.setFieldSetMapper(createFieldMapper());
        return defaultLineMapper;
    }

    private LineTokenizer createLineTokenizer() {
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer(DELIMITER);
        delimitedLineTokenizer.setNames(new String[]{"name", "alcoholVolume", "price"});
        return delimitedLineTokenizer;
    }

    private FieldSetMapper<Beer> createFieldMapper() {
        BeanWrapperFieldSetMapper<Beer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Beer.class);
        return fieldSetMapper;
    }

    @Bean
    public ItemWriter<Beer> importWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Beer> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public Step exportStep(ItemReader<Beer> exportItemReader,
                           ItemWriter<Beer> exportItemWriter) {
        return stepBuilderFactory.get("exportStep")
                .<Beer, Beer>chunk(10)
                .reader(exportItemReader)
                .writer(exportItemWriter)
                .build();
    }

    @Bean
    public ItemStreamReader<Beer> exportItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader reader = new JpaPagingItemReader();
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("FROM Beer WHERE rating IS NOT NULL");
        reader.setPageSize(10);
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Beer> exportItemWriter(@Value("#{jobParameters[outputFile]}") String pathToFile) {
        FlatFileItemWriter<Beer> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(new FileSystemResource(pathToFile));
        flatFileItemWriter.setLineAggregator(createBeerLineAggregator());
        flatFileItemWriter.setEncoding(UTF_8);
        return flatFileItemWriter;
    }

    private LineAggregator<Beer> createBeerLineAggregator() {
        DelimitedLineAggregator<Beer> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        lineAggregator.setFieldExtractor(createBeerFieldExtractor());
        return lineAggregator;
    }

    private FieldExtractor<Beer> createBeerFieldExtractor() {
        BeanWrapperFieldExtractor<Beer> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[]{"name", "rating"});
        return extractor;
    }

}
