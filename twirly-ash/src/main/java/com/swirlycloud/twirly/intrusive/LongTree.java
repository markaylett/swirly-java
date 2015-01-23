/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class LongTree<T> extends Tree<T> {

    protected static int compareKey(long lhs, long rhs) {
        int i;
        if (lhs < rhs) {
            i = -1;
        } else if (lhs > rhs) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

    protected abstract int compareKey(T lhs, long rhs);

    /**
     * Finds the node with the same key as node.
     */

    public final T find(long key) {
        T tmp = root;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, key);
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

    public final T nfind(long key) {
        T tmp = root;
        T res = null;
        int comp;
        while (tmp != null) {
            comp = compareKey(tmp, key);
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

    public final T pfind(long key) {
        T tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = compareKey(tmp, key);
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
}
