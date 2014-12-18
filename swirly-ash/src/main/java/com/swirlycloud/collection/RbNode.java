/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

public interface RbNode {

    RbNode setLeft(RbNode left);

    RbNode setRight(RbNode right);

    RbNode setParent(RbNode parent);

    void setColor(int color);

    RbNode rbNext();

    RbNode rbPrev();

    long getKey();

    RbNode getLeft();

    RbNode getRight();

    RbNode getParent();

    int getColor();
}