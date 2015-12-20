/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.node;

/**
 * Red-black node.
 * 
 * @author Mark Aylett
 */
public interface RbNode {

    void setNode(RbNode rhs);

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
