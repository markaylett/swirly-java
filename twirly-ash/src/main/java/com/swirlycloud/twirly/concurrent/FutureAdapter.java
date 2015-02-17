/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.concurrent;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class FutureAdapter<V> implements Future<V> {
    private final V value;

    public FutureAdapter(V value) {
        this.value = value;
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public final boolean isCancelled() {
        return false;
    }

    @Override
    public final boolean isDone() {
        return true;
    }

    @Override
    public final V get() {
        return value;
    }

    @Override
    public final V get(long timeout, TimeUnit unit) {
        return value;
    }
}
