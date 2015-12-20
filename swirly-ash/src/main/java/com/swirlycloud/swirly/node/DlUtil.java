/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.node;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

public final @NonNullByDefault class DlUtil {
    private DlUtil() {
    }

    public static final DlNode NULL = new DlNode() {

        @Override
        public final void insert(DlNode prev, DlNode next) {
            throw new UnsupportedOperationException("insert");
        }

        @Override
        public final void insertBefore(DlNode next) {
            throw new UnsupportedOperationException("insertBefore");
        }

        @Override
        public final void insertAfter(DlNode prev) {
            throw new UnsupportedOperationException("insertAfter");
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public final void setDlPrev(@NonNull DlNode prev) {
            throw new UnsupportedOperationException("setDlPrev");
        }

        @Override
        public final void setDlNext(@NonNull DlNode next) {
            throw new UnsupportedOperationException("setDlNext");
        }

        @Override
        public final DlNode dlPrev() {
            throw new UnsupportedOperationException("dlPrev");
        }

        @Override
        public final DlNode dlNext() {
            throw new UnsupportedOperationException("dlNext");
        }

        @Override
        public final boolean isEnd() {
            return true;
        }
    };
}
