/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import static com.swirlycloud.swirly.util.CollectionUtil.compareLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.intrusive.AbstractTree;

/**
 * Request tree keyed by market and id. Requests are identified by market and id only, so instances
 * should not be used as heterogeneous Request containers, where Requests of different types may
 * share the same identity.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class RequestIdTree extends AbstractTree<Request> {

    private static final long serialVersionUID = 1L;

    @Override
    protected final int compareNode(Request lhs, Request rhs) {
        return compareKey(lhs, rhs.getMarket(), rhs.getId());
    }

    protected final int compareKey(Request lhs, String market, long id) {
        int n = lhs.getMarket().compareTo(market);
        if (n == 0) {
            n = compareLong(lhs.getId(), id);
        }
        return n;
    }

    public final @Nullable Request find(String market, long id) {
        Request tmp = root;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, market, id);
            if (comp > 0) {
                tmp = getLeft(tmp);
            } else if (comp < 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Finds the first node greater than or equal to the search key.
     */
    public final @Nullable Request nfind(String market, long id) {
        Request tmp = root;
        Request res = null;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, market, id);
            if (comp > 0) {
                res = tmp;
                tmp = getLeft(tmp);
            } else if (comp < 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return res;
    }

    // Extensions.

    /**
     * Return match or parent.
     */
    public final @Nullable Request pfind(String market, long id) {
        Request tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = compareKey(tmp, market, id);
            if (comp > 0) {
                tmp = getLeft(tmp);
            } else if (comp < 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return parent;
    }

    @Override
    protected final void setNode(Request lhs, Request rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(Request node, @Nullable Request left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(Request node, @Nullable Request right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(Request node, @Nullable Request parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(Request node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable Request next(Request node) {
        return (Request) node.rbNext();
    }

    @Override
    protected final @Nullable Request prev(Request node) {
        return (Request) node.rbPrev();
    }

    @Override
    protected final @Nullable Request getLeft(Request node) {
        return (Request) node.getLeft();
    }

    @Override
    protected final @Nullable Request getRight(Request node) {
        return (Request) node.getRight();
    }

    @Override
    protected final @Nullable Request getParent(Request node) {
        return (Request) node.getParent();
    }

    @Override
    protected final int getColor(Request node) {
        return node.getColor();
    }
}
