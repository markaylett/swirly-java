/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import static java.util.Collections.binarySearch;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A sorted and unique set of elements. Although this collection could be considered a
 * {@link java.util.List} or {@link java.util.Set}, neither interface is currently implemented.
 *
 * @author Mark Aylett
 * @param <V>
 *            The element type.
 */
public final class SortedList<V> implements Collection<V> {

    private final LinkedList<V> list;
    private final Comparator<? super V> comp;

    public SortedList(Comparator<? super V> comp) {
        this.comp = comp;
        this.list = new LinkedList<>();
    }

    // Collection.

    @Override
    public final boolean add(V element) {
        addElement(element);
        return true;
    }

    @Override
    public final boolean addAll(Collection<? extends V> c) {
        for (final V node : c) {
            add(node);
        }
        return true;
    }

    @Override
    public final void clear() {
        list.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean contains(Object o) {
        return 0 <= containsElement((V) o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean containsAll(Collection<?> c) {
        for (final Object o : c) {
            if (containsElement((V) o) < 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public final Iterator<V> iterator() {
        return list.iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final boolean remove(Object o) {
        return 0 <= removeElement((V) o);
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (final Object o : c) {
            if (remove(o)) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public final int size() {
        return list.size();
    }

    @Override
    public final Object[] toArray() {
        return list.toArray();
    }

    @Override
    public final <U> U[] toArray(U[] a) {
        return list.toArray(a);
    }

    // This.

    public final int addElement(V element) {

        // Index of the search key, if it is contained in the list; otherwise,
        // (-(insertion point) - 1).

        int i = binarySearch(list, element, comp);
        if (i < 0) {
            i = -i - 1;
            list.add(i, element);
            return i;
        }

        list.set(i, element);
        return i;
    }

    public final int containsElement(V element) {
        return binarySearch(list, element, comp);
    }

    public final int removeElement(V element) {
        final int i = binarySearch(list, element, comp);
        if (0 <= i) {
            list.remove(i);
        }
        return i;
    }

    public final V removeFirst() {
        return list.removeFirst();
    }

    public final V removeLast() {
        return list.removeLast();
    }

    public final V get(int i) {
        return list.get(i);
    }

    public final V getFirst() {
        return list.getFirst();
    }

    public final V getLast() {
        return list.getLast();
    }
}
