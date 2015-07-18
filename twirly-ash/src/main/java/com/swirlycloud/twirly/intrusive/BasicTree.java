/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Tree with a single key derived from Object.
 * 
 * @param <K>
 *            The key type.
 * @param <V>
 *            The element or value type.
 * 
 * @author Mark Aylett
 */
public abstract @NonNullByDefault class BasicTree<K, V> extends Tree<V> {

    protected abstract int compareKeyDirect(V lhs, K rhs);

    /**
     * Finds the node with the same key as node.
     */
    public final @Nullable V find(K key) {
        V tmp = root;
        int comp;
        while (tmp != null) {
            comp = compareKeyDirect(tmp, key);
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
    public final @Nullable V nfind(K key) {
        V tmp = root;
        V res = null;
        int comp;
        while (tmp != null) {
            comp = compareKeyDirect(tmp, key);
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
    public final @Nullable V pfind(K key) {
        V tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = compareKeyDirect(tmp, key);
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
