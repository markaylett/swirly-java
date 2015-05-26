/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public interface TransNode extends SlNode {

    /**
     * @param next
     *            The next link in the list.
     */
    void setTransNext(TransNode next);

    TransNode transNext();
}
