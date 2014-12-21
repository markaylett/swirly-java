/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

public final class EmailIdx {

    private final Trader[] buckets;

    private static boolean equals(Trader trader, String email) {
        assert email != null;
        return trader.getEmail().equals(email);
    }

    private static int indexFor(int hash, int length) {
        return (hash & 0x7fffffff) % length;
    }

    public EmailIdx(int nBuckets) {
        assert nBuckets > 0;
        this.buckets = new Trader[nBuckets];
    }

    public final void insert(Trader trader) {
        assert trader != null;
        if (trader.getEmail() != null) {
            final int bucket = indexFor(trader.getEmail().hashCode(), buckets.length);
            trader.emailNext = buckets[bucket];
            buckets[bucket] = trader;
        }
    }

    public final Trader find(String email) {
        assert email != null;
        final int bucket = indexFor(email.hashCode(), buckets.length);
        for (Trader it = buckets[bucket]; it != null; it = it.emailNext) {
            if (equals(it, email)) {
                return it;
            }
        }
        return null;
    }
}
