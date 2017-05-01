package com.tgimbut.springbatch.configuration;

import com.tgimbut.springbatch.common.BeerProcessorListener;
import com.tgimbut.springbatch.common.ModuloPartitioner;
import com.tgimbut.springbatch.common.RatingProcessor;
import com.tgimbut.springbatch.domain.Beer;
import com.tgimbut.springbatch.service.NoAlcoholException;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile({"localPartitionedStep"})
public class PartitionedRatingStepConfiguration {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private TaskExecutor taskExecutor;

    @Bean
    public Step ratingStep(Partitioner moduloPartitioner,
                           PartitionHandler partitionHandler) {
        return stepBuilderFactory.get("ratingStep")
                .partitioner("ratingStepSlave", moduloPartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    public PartitionHandler partitionHandler(Step ratingStepSlave)
            throws Exception {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();

        partitionHandler.setGridSize(3);
        partitionHandler.setTaskExecutor(taskExecutor);
        partitionHandler.setStep(ratingStepSlave);

        return partitionHandler;
    }

    @Bean
    public Step ratingStepSlave(ItemStreamReader<Beer> ratingReader,
                                RatingProcessor ratingProcessor,
                                ItemWriter<Beer> ratingWriter,
                                BeerProcessorListener beerProcessorListener) {
        return stepBuilderFactory.get("ratingStepSlave")
                .<Beer, Beer>chunk(10)
                .reader(ratingReader)
                .processor(ratingProcessor)
                .writer(ratingWriter)
                .faultTolerant()
                .skipLimit(10000)
                .skip(NoAlcoholException.class)
                .listener(beerProcessorListener)
                .throttleLimit(1)
                .build();
    }

    @Bean
    @StepScope
    public ItemStreamReader<Beer> ratingReader(@Value("#{stepExecutionContext['divisor']}") Integer modDivisor,
                                               @Value("#{stepExecutionContext['remainder']}") Integer modRemainder) {
        HibernateCursorItemReader<Beer> itemReader = new HibernateCursorItemReader<>();
        itemReader.setSessionFactory(sessionFactory);
        itemReader.setQueryString("FROM Beer WHERE rating IS NULL" +
                " AND mod(id, :divisor) = :remainder");
        itemReader.setParameterValues(getParametersMap(modDivisor, modRemainder));
        itemReader.setUseStatelessSession(true);
        itemReader.setFetchSize(10);
        return itemReader;
    }

    private Map<String, Object> getParametersMap(Integer divisor,
                                                 Integer remainder) {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("divisor", divisor);
        parameterValues.put("remainder", remainder);
        return parameterValues;
    }

    @Bean
    public ItemWriter<Beer> ratingWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Beer> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    public ModuloPartitioner moduloPartitioner() {
        return new ModuloPartitioner();
    }
}
