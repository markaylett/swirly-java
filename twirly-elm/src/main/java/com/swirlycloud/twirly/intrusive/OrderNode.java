/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class OrderNode extends BasicRbNode implements DlNode {

    private transient DlNode prev;
    private transient DlNode next;

    @Override
    public final void insert(DlNode prev, DlNode next) {

        assert prev != null;
        assert next != null;

        prev.setDlNext(this);
        this.setDlPrev(prev);

        next.setDlPrev(this);
        this.setDlNext(next);
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
        dlNext().setDlPrev(prev);
        dlPrev().setDlNext(next);
        setDlPrev(null);
        setDlNext(null);
    }

    @Override
    public void setDlPrev(DlNode prev) {
        this.prev = prev;
    }

    @Override
    public void setDlNext(DlNode next) {
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
