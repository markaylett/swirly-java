/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

public abstract class BasicRbDlNode extends BasicRbNode implements DlNode {

    private transient DlNode prev;
    private transient DlNode next;

    @Override
    public final void insert(DlNode prev, DlNode next) {

        assert prev != null;
        assert next != null;

        prev.setNext(this);
        this.setPrev(prev);

        next.setPrev(this);
        this.setNext(next);
    }

    @Override
    public final void insertBefore(DlNode next) {
        assert next != null;
        insert(next.dlPrev(), next);
    }

    @Override
    public final void insertAfter(DlNode prev) {
        assert prev != null;
        insert(prev, prev.dlNext());
    }

    @Override
    public final void remove() {
        dlNext().setPrev(prev);
        dlPrev().setNext(next);
        setPrev(null);
        setNext(null);
    }

    @Override
    public void setPrev(DlNode prev) {
        this.prev = prev;
    }

    @Override
    public void setNext(DlNode next) {
        this.next = next;
    }

    @Override
    public final DlNode dlNext() {
        return this.next;
    }

    @Override
    public final DlNode dlPrev() {
        return this.prev;
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
