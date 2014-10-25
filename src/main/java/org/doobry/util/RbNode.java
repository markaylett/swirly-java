/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

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