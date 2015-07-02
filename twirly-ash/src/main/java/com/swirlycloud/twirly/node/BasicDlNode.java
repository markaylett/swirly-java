/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import org.eclipse.jdt.annotation.NonNull;

public abstract class BasicDlNode implements DlNode {

    private transient @NonNull DlNode prev = DlUtil.NULL;
    private transient @NonNull DlNode next = DlUtil.NULL;

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
        setDlPrev(DlUtil.NULL);
        setDlNext(DlUtil.NULL);
    }

    @Override
    public void setDlPrev(@NonNull DlNode prev) {
        this.prev = prev;
    }

    @Override
    public void setDlNext(@NonNull DlNode next) {
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
