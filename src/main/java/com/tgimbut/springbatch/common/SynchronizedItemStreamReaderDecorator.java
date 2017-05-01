package com.tgimbut.springbatch.common;

import org.springframework.batch.item.*;

public class SynchronizedItemStreamReaderDecorator<T> implements ItemStreamReader<T> {

    private final ItemReader<T> delegate;

    public SynchronizedItemStreamReaderDecorator(ItemReader<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized T read() throws Exception {
        return delegate.read();
    }

    @Override
    public synchronized void open(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).open(executionContext);
        }
    }

    @Override
    public synchronized void update(ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).update(executionContext);
        }
    }

    @Override
    public synchronized void close() throws ItemStreamException {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).close();
        }
    }

}
