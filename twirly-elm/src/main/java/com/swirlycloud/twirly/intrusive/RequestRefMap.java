/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.NullUtil.emptyIfNull;

import com.swirlycloud.twirly.domain.Request;

public final class RequestRefMap extends Map<Request> {

    private static int hashKey(String trader, String ref) {
        final int prime = 31;
        int result = 1;
        result = prime * result + trader.hashCode();
        result = prime * result + ref.hashCode();
        return result;
    }

    private static boolean equalKey(Request lhs, String trader, String ref) {
        return lhs.getTrader().equals(trader) && emptyIfNull(lhs.getRef()).equals(ref);
    }

    @Override
    protected final void setNext(Request node, Request next) {
        node.setRefNext(next);
    }

    @Override
    protected final Request next(Request node) {
        return node.refNext();
    }

    @Override
    protected final int hashNode(Request node) {
        return hashKey(node.getTrader(), node.getRef());
    }

    @Override
    protected final boolean equalNode(Request lhs, Request rhs) {
        return lhs.getTrader().equals(rhs.getTrader())
                && emptyIfNull(lhs.getRef()).equals(rhs.getRef());
    }

    public RequestRefMap(int capacity) {
        super(capacity);
    }

    public final Request remove(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKey(trader, ref), buckets.length);
        Request it = getBucket(i);
        if (it == null) {
            return null;
        }
        // Check if the first element in the bucket has an equivalent key.
        if (equalKey(it, trader, ref)) {
            buckets[i] = next(it);
            --size;
            return it;
        }
        // Check if a subsequent element in the bucket has an equivalent key.
        for (Request next; (next = next(it)) != null; it = next) {
            if (equalKey(next, trader, ref)) {
                setNext(it, next(next));
                --size;
                return next;
            }
        }
        return null;
    }

    public final Request find(String trader, String ref) {
        if (isEmpty()) {
            return null;
        }
        final int i = indexFor(hashKey(trader, ref), buckets.length);
        for (Request it = getBucket(i); it != null; it = next(it)) {
            if (equalKey(it, trader, ref)) {
                return it;
            }
        }
        return null;
    }
}
