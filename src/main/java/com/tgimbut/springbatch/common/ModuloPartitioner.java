package com.tgimbut.springbatch.common;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public final class ModuloPartitioner implements Partitioner {

    private static final String DIVISOR = "divisor";

    private static final String REMAINDER = "remainder";

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        final Map<String, ExecutionContext> contextMap = new HashMap<>();

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt(DIVISOR, gridSize);
            context.putInt(REMAINDER, i);
            contextMap.put(getPartitionName(i), context);
        }

        return contextMap;
    }

    private String getPartitionName(int index) {
        return String.format("partition-%d", index);
    }
}
