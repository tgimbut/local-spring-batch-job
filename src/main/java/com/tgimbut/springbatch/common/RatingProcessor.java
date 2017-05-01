package com.tgimbut.springbatch.common;

import com.tgimbut.springbatch.domain.Beer;
import com.tgimbut.springbatch.service.BeerService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RatingProcessor implements ItemProcessor<Beer, Beer> {

    @Autowired
    private BeerService beerService;

    @Override
    public Beer process(Beer item) throws Exception {
        Double rating = beerService.ratingBeer(item);
        item.setRating(rating);
        return item;
    }
}