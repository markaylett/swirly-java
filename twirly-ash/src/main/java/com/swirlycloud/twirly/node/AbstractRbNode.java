/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class AbstractRbNode implements Serializable, RbNode {

    private static final long serialVersionUID = 1L;

    private RbNode left;
    private RbNode right;
    private transient RbNode parent;
    private int color;

    private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (left != null) {
            left.setParent(this);
        }
        if (right != null) {
            right.setParent(this);
        }
    }

    private final void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Override
    public final void setNode(RbNode rhs) {
        this.left = rhs.getLeft();
        this.right = rhs.getRight();
        this.parent = rhs.getParent();
        this.color = rhs.getColor();
    }

    @Override
    public final RbNode setLeft(RbNode left) {
        this.left = left;
        return left;
    }

    @Override
    public final RbNode setRight(RbNode right) {
        this.right = right;
        return right;
    }

    @Override
    public final RbNode setParent(RbNode parent) {
        this.parent = parent;
        return parent;
    }

    @Override
    public final void setColor(int color) {
        this.color = color;
    }

    @Override
    public final RbNode rbNext() {
        return RbUtil.rbNext(this);
    }

    @Override
    public final RbNode rbPrev() {
        return RbUtil.rbPrev(this);
    }

    @Override
    public final RbNode getLeft() {
        return left;
    }

    @Override
    public final RbNode getRight() {
        return right;
    }

    @Override
    public final RbNode getParent() {
        return parent;
    }

    @Override
    public final int getColor() {
        return color;
    }
}
