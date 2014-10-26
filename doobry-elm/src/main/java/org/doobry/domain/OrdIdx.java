/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.domain;

public final class OrdIdx {

    private final Order[] buckets;

    private static boolean equals(Order order, long trid, String ref) {
        assert ref != null;
        return order.getTrader().getId() == trid && order.getRef().equals(ref);
    }

    private static int hashCode(long id) {
        return (int) (id ^ id >>> 32);
    }

    private static int indexOf(int hash, int length) {
        return hash & length - 1;
    }

    private static int hashCode(long trid, String ref) {
        assert ref != null;
        final int prime = 31;
        int result = 1;
        result = prime * result + hashCode(trid);
        result = prime * result + ref.hashCode();
        return result;
    }

    public OrdIdx(int nBuckets) {
        assert nBuckets > 0;
        this.buckets = new Order[nBuckets];
    }

    public final void insert(Order order) {
        assert order != null;
        if (order.getRef() != null) {
            final int bucket = indexOf(hashCode(order.getTrader().getId(), order.getRef()),
                    this.buckets.length);
            order.nextRef = this.buckets[bucket];
            this.buckets[bucket] = order;
        }
    }

    public final Order remove(long trid, String ref) {
        assert ref != null;
        final int bucket = indexOf(hashCode(trid, ref), this.buckets.length);
        Order it = this.buckets[bucket];
        if (it == null) {
            return null;
        }
        if (equals(it, trid, ref)) {
            this.buckets[bucket] = it.nextRef;
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
        final int bucket = indexOf(hashCode(trid, ref), this.buckets.length);
        for (Order it = this.buckets[bucket]; it != null; it = it.nextRef) {
            if (equals(it, trid, ref)) {
                return it;
            }
        }
        return null;
    }
}
