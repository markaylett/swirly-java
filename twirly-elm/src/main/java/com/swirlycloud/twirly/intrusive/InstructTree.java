/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.node.RbUtil.compareLong;

import com.swirlycloud.twirly.domain.Instruct;
import com.swirlycloud.twirly.node.RbNode;

public final class InstructTree extends Tree<RbNode> {

    @Override
    protected final int compareKey(RbNode lhs, RbNode rhs) {
        final Instruct r = (Instruct) rhs;
        return compareKey(lhs, r.getMarket(), r.getId());
    }

    protected final int compareKey(RbNode lhs, String market, long id) {
        final Instruct l = (Instruct) lhs;
        int n = l.getMarket().compareTo(market);
        if (n == 0) {
            n = compareLong(l.getId(), id);
        }
        return n;
    }

    public final RbNode find(String market, long id) {
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

    public final RbNode nfind(String market, long id) {
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

    public final RbNode pfind(String market, long id) {
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
    protected final void setNode(RbNode node, RbNode left, RbNode right, RbNode parent, int color) {
        node.setNode(left, right, parent, color);
    }

    @Override
    protected final RbNode setLeft(RbNode node, RbNode left) {
        return node.setLeft(left);
    }

    @Override
    protected final RbNode setRight(RbNode node, RbNode right) {
        return node.setRight(right);
    }

    @Override
    protected final RbNode setParent(RbNode node, RbNode parent) {
        return node.setParent(parent);
    }

    @Override
    protected final void setColor(RbNode node, int color) {
        node.setColor(color);
    }

    @Override
    protected final RbNode next(RbNode node) {
        return node.rbNext();
    }

    @Override
    protected final RbNode prev(RbNode node) {
        return node.rbPrev();
    }

    @Override
    protected final RbNode getLeft(RbNode node) {
        return node.getLeft();
    }

    @Override
    protected final RbNode getRight(RbNode node) {
        return node.getRight();
    }

    @Override
    protected final RbNode getParent(RbNode node) {
        return node.getParent();
    }

    @Override
    protected final int getColor(RbNode node) {
        return node.getColor();
    }
}
