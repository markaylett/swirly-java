/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.compareInt;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Posn;

public final @NonNullByDefault class PosnTree extends AbstractTree<Posn> {

    private static final long serialVersionUID = 1L;

    @Override
    protected final int compareNode(Posn lhs, Posn rhs) {
        return compareKey(lhs, rhs.getTrader(), rhs.getContr(), rhs.getSettlDay());
    }

    protected final int compareKey(Posn lhs, String trader, String contr, int settlDay) {
        int n = lhs.getTrader().compareTo(trader);
        if (n == 0) {
            n = lhs.getContr().compareTo(contr);
            if (n == 0) {
                n = compareInt(lhs.getSettlDay(), settlDay);
            }
        }
        return n;
    }

    public final @Nullable Posn find(String trader, String contr, int settlDay) {
        Posn tmp = root;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, trader, contr, settlDay);
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
    public final @Nullable Posn nfind(String trader, String contr, int settlDay) {
        Posn tmp = root;
        Posn res = null;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, trader, contr, settlDay);
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
    public final @Nullable Posn pfind(String trader, String contr, int settlDay) {
        Posn tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = compareKey(tmp, trader, contr, settlDay);
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
    protected final void setNode(Posn lhs, Posn rhs) {
        lhs.setNode(rhs);
    }

    @Override
    protected final void setLeft(Posn node, @Nullable Posn left) {
        node.setLeft(left);
    }

    @Override
    protected final void setRight(Posn node, @Nullable Posn right) {
        node.setRight(right);
    }

    @Override
    protected final void setParent(Posn node, @Nullable Posn parent) {
        node.setParent(parent);
    }

    @Override
    protected final void setColor(Posn node, int color) {
        node.setColor(color);
    }

    @Override
    protected final @Nullable Posn next(Posn node) {
        return (Posn) node.rbNext();
    }

    @Override
    protected final @Nullable Posn prev(Posn node) {
        return (Posn) node.rbPrev();
    }

    @Override
    protected final @Nullable Posn getLeft(Posn node) {
        return (Posn) node.getLeft();
    }

    @Override
    protected final @Nullable Posn getRight(Posn node) {
        return (Posn) node.getRight();
    }

    @Override
    protected final @Nullable Posn getParent(Posn node) {
        return (Posn) node.getParent();
    }

    @Override
    protected final int getColor(Posn node) {
        return node.getColor();
    }
}
