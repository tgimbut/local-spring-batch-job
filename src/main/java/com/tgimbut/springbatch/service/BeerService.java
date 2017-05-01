package com.tgimbut.springbatch.service;

import com.tgimbut.springbatch.domain.Beer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BeerService {

    private static final Logger log = LoggerFactory.getLogger(BeerService.class);

    public double ratingBeer(Beer beer) throws InterruptedException, NoAlcoholException {
        BigDecimal alcoholVolume = BigDecimal.valueOf(beer.getAlcoholVolume());

        if (alcoholVolume.equals(BigDecimal.valueOf(0.0))) {
            throw new NoAlcoholException();
        }
        sleepingAfterConsumption(alcoholVolume);

        BigDecimal price = BigDecimal.valueOf(beer.getPrice());
        double result = alcoholVolume.movePointRight(2)
                .divide(price, 2, BigDecimal.ROUND_UP)
                .doubleValue();
        return result;
    }

    private void sleepingAfterConsumption(BigDecimal alcoholVolume) throws InterruptedException {
        long millis = alcoholVolume.movePointRight(2).longValue();
        log.debug("sleep time: " + millis);
        Thread.sleep(millis);
    }
}
