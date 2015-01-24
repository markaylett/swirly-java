/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public interface RbNode {

    void setNode(RbNode left, RbNode right, RbNode parent, int color);

    RbNode setLeft(RbNode left);

    RbNode setRight(RbNode right);

    RbNode setParent(RbNode parent);

    void setColor(int color);

    RbNode rbNext();

    RbNode rbPrev();

    RbNode getLeft();

    RbNode getRight();

    RbNode getParent();

    int getColor();
}
