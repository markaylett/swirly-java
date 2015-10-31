/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public abstract @NonNullByDefault class AbstractLongTree<V> extends AbstractTree<V> {

    private static final long serialVersionUID = 1L;

    protected abstract int compareKey(V lhs, long rhs);

    /**
     * Finds the node with the same key as node.
     */
    public final @Nullable V find(long key) {
        V tmp = root;
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
    public final @Nullable V nfind(long key) {
        V tmp = root;
        V res = null;
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
    public final @Nullable V pfind(long key) {
        V tmp = root, parent = null;
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
