/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.node.SlNode;

public final class RefHashTable extends HashTable<SlNode> {

    private static int hashKeyDirect(String trader, String ref) {
        final int prime = 31;
        int result = 1;
        result = prime * result + trader.hashCode();
        result = prime * result + ref.hashCode();
        return result;
    }

    private static boolean equalKeyDirect(SlNode lhs, String trader, String ref) {
        final Order lorder = (Order) lhs;
        return lorder.getTrader().equals(trader) && lorder.getRef().equals(ref);
    }

    @Override
    protected final void setNext(SlNode node, SlNode next) {
        node.setSlNext(next);
    }

    @Override
    protected final SlNode next(SlNode node) {
        return node.slNext();
    }

    @Override
    protected final int hashKey(SlNode node) {
        final Order order = (Order) node;
        return hashKeyDirect(order.getTrader(), order.getRef());
    }

    @Override
    protected final boolean equalKey(SlNode lhs, SlNode rhs) {
        final Order lorder = (Order) lhs;
        final Order rorder = (Order) rhs;
        return lorder.getTrader().equals(rorder.getTrader())
                && lorder.getRef().equals(rorder.getRef());
    }

    public RefHashTable(int capacity) {
        super(capacity);
    }

    public final SlNode remove(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        int i = indexFor(hashKeyDirect(trader, ref), buckets.length);
        SlNode it = getBucket(i);
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
        for (SlNode next; (next = next(it)) != null; it = next) {
            if (equalKeyDirect(next, trader, ref)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final SlNode find(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKeyDirect(trader, ref), buckets.length);
        for (SlNode it = getBucket(i); it != null; it = next(it)) {
            if (equalKeyDirect(it, trader, ref)) {
                return it;
            }
        }
        return null;
    }
}
