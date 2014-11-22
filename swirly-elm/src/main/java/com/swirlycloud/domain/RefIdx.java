/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.domain;

public final class RefIdx {

    private final Order[] buckets;

    private static boolean equals(Order order, long trid, String ref) {
        assert ref != null;
        return order.getUser().getId() == trid && order.getRef().equals(ref);
    }

    private static int hashCode(long id) {
        return (int) (id ^ id >>> 32);
    }

    private static int hashCode(long trid, String ref) {
        assert ref != null;
        final int prime = 31;
        int result = 1;
        result = prime * result + hashCode(trid);
        result = prime * result + ref.hashCode();
        return result;
    }

    private static int indexFor(int hash, int length) {
        return (hash & 0x7fffffff) % length;
    }

    public RefIdx(int nBuckets) {
        assert nBuckets > 0;
        this.buckets = new Order[nBuckets];
    }

    public final void insert(Order order) {
        assert order != null;
        if (order.getRef() != null) {
            final int bucket = indexFor(hashCode(order.getUser().getId(), order.getRef()),
                    buckets.length);
            order.nextRef = buckets[bucket];
            buckets[bucket] = order;
        }
    }

    public final Order remove(long trid, String ref) {
        assert ref != null;
        final int bucket = indexFor(hashCode(trid, ref), this.buckets.length);
        Order it = buckets[bucket];
        if (it == null) {
            return null;
        }
        if (equals(it, trid, ref)) {
            buckets[bucket] = it.nextRef;
            return it;
        }
        for (; it.nextRef != null; it = it.nextRef) {
            final Order next = it.nextRef;
            if (equals(next, trid, ref)) {
                it.nextRef = next.nextRef;
                return next;
            }
        }
        return null;
    }

    public final Order find(long trid, String ref) {
        assert ref != null;
        final int bucket = indexFor(hashCode(trid, ref), buckets.length);
        for (Order it = buckets[bucket]; it != null; it = it.nextRef) {
            if (equals(it, trid, ref)) {
                return it;
            }
        }
        return null;
    }
}
