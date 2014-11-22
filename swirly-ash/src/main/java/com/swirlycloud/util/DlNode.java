/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

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
