/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

public abstract @NonNullByDefault class AbstractDlNode implements DlNode {

    private transient DlNode prev = DlUtil.NULL;
    private transient DlNode next = DlUtil.NULL;

    @Override
    public final void insert(DlNode prev, DlNode next) {

        prev.setDlNext(this);
        this.setDlPrev(prev);

        next.setDlPrev(this);
        this.setDlNext(next);
    }

    @Override
    public final void insertBefore(DlNode next) {
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
    public final DlNode dlPrev() {
        return this.prev;
    }

    @Override
    public final DlNode dlNext() {
        return this.next;
    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
