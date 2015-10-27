/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Request;
import com.swirlycloud.twirly.node.RbNode;

public final @NonNullByDefault class RequestTree extends Tree<RbNode> {

    private static final long serialVersionUID = 1L;

    @Override
    protected final int compareKey(RbNode lhs, RbNode rhs) {
        final Request r = (Request) rhs;
        return compareKey(lhs, r.getMarket(), r.getId());
    }

    protected final int compareKey(RbNode lhs, String market, long id) {
        final Request l = (Request) lhs;
        int n = l.getMarket().compareTo(market);
        if (n == 0) {
            n = compareLong(l.getId(), id);
        }
        return n;
    }

    public final @Nullable RbNode find(String market, long id) {
        RbNode tmp = root;
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
    public final @Nullable RbNode nfind(String market, long id) {
        RbNode tmp = root;
        RbNode res = null;
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
    public final @Nullable RbNode pfind(String market, long id) {
        RbNode tmp = root, parent = null;
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
    protected final void setNode(RbNode lhs, RbNode rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(RbNode node, @Nullable RbNode left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(RbNode node, @Nullable RbNode right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(RbNode node, @Nullable RbNode parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(RbNode node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable RbNode next(RbNode node) {
        return node.rbNext();
    }

    @Override
    protected final @Nullable RbNode prev(RbNode node) {
        return node.rbPrev();
    }

    @Override
    protected final @Nullable RbNode getLeft(RbNode node) {
        return node.getLeft();
    }

    @Override
    protected final @Nullable RbNode getRight(RbNode node) {
        return node.getRight();
    }

    @Override
    protected final @Nullable RbNode getParent(RbNode node) {
        return node.getParent();
    }

    @Override
    protected final int getColor(RbNode node) {
        return node.getColor();
    }
}
