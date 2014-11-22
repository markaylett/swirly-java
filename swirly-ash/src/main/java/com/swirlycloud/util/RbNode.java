/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

public interface RbNode extends Identifiable {

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