package com.tgimbut.springbatch.common;

import com.tgimbut.springbatch.domain.Beer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterProcess;
import org.springframework.batch.core.annotation.BeforeProcess;
import org.springframework.batch.core.annotation.OnProcessError;
import org.springframework.stereotype.Component;

@Component
public class BeerProcessorListener {

    private static final Logger log = LoggerFactory.getLogger(BeerProcessorListener.class);

    @BeforeProcess
    public void beforeProcess(Beer item) {
        log.info("Starting processing: {}", item);
    }

    @AfterProcess
    public void afterProcess(Beer item, Beer result) {
        log.info("Processed: {} - computed rating = {}", result, result.getRating());
    }

    @OnProcessError
    public void onProcessError(Beer item, java.lang.Exception e) {
        log.error("Problem with item: '{}'. Cause: '{}'", item, e.getMessage());
    }

}
