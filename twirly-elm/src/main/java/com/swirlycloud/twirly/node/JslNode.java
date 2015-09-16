/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

/**
 * Singly-linked node that may be updated asynchronously by a {@code Journ}.
 * 
 * @author Mark Aylett
 */
public interface JslNode {

    /**
     * @param next
     *            The next link in the list.
     */
    void setJslNext(JslNode next);

    JslNode jslNext();
}
