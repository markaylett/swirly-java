/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.NullUtil.emptyIfNull;

import com.swirlycloud.twirly.domain.Order;

public final class RefHashTable extends HashTable<Order> {

    private static int hashKeyDirect(String trader, String ref) {
        final int prime = 31;
        int result = 1;
        result = prime * result + trader.hashCode();
        result = prime * result + ref.hashCode();
        return result;
    }

    private static boolean equalKeyDirect(Order lhs, String trader, String ref) {
        return lhs.getTrader().equals(trader) && emptyIfNull(lhs.getRef()).equals(ref);
    }

    @Override
    protected final void setNext(Order node, Order next) {
        node.setRefNext(next);
    }

    @Override
    protected final Order next(Order node) {
        return node.refNext();
    }

    @Override
    protected final int hashKey(Order node) {
        return hashKeyDirect(node.getTrader(), node.getRef());
    }

    @Override
    protected final boolean equalKey(Order lhs, Order rhs) {
        return lhs.getTrader().equals(rhs.getTrader())
                && emptyIfNull(lhs.getRef()).equals(rhs.getRef());
    }

    public RefHashTable(int capacity) {
        super(capacity);
    }

    public final Order remove(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKeyDirect(trader, ref), buckets.length);
        Order it = getBucket(i);
        if (it == null) {
            return null;
        }
        // Check if the first element in the bucket has an equivalent key.
        if (equalKeyDirect(it, trader, ref)) {
            buckets[i] = next(it);
            --size;
            return it;
        }
        // Check if a subsequent element in the bucket has an equivalent key.
        for (Order next; (next = next(it)) != null; it = next) {
            if (equalKeyDirect(next, trader, ref)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final Order find(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKeyDirect(trader, ref), buckets.length);
        for (Order it = getBucket(i); it != null; it = next(it)) {
            if (equalKeyDirect(it, trader, ref)) {
                return it;
            }
        }
        return null;
    }
}
