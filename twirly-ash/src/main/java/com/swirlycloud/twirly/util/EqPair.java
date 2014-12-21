/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import javax.annotation.concurrent.Immutable;

@Immutable
public class EqPair<T, U> implements Pair<T, U> {
    protected final T first;
    protected final U second;

    public EqPair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof EqPair<?, ?>) {
            final EqPair<?, ?> rhs = (EqPair<?, ?>) obj;
            return first.equals(rhs.first) && second.equals(rhs.second);
        }
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s)", first, second);
    }

    @Override
    public final T getFirst() {
        return first;
    }

    @Override
    public final U getSecond() {
        return second;
    }
}
