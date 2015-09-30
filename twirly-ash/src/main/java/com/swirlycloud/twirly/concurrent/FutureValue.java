package com.swirlycloud.twirly.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Future with external setter.
 * 
 * @author Mark Aylett
 * @param <V>
 *            The contained type.
 */
public class FutureValue<V> extends FutureTask<V> {
    private static final Callable<Object> UNUSED = new Callable<Object>() {
        @Override
        public final Object call() {
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    public FutureValue() {
        super((Callable<V>) UNUSED);
    }

    @Override
    public final void set(V value) {
        super.set(value);
    }

    @Override
    public final void setException(Throwable t) {
        super.setException(t);
    }
}
