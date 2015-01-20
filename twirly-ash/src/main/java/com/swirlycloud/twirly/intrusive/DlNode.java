/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public interface DlNode {

    void insert(DlNode prev, DlNode next);

    void insertBefore(DlNode next);

    void insertAfter(DlNode prev);

    void remove();

    void setDlPrev(DlNode prev);

    void setDlNext(DlNode next);

    DlNode dlNext();

    DlNode dlPrev();

    boolean isEnd();
}
