/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public abstract class BasicRbNode implements RbNode {

    private transient RbNode left;
    private transient RbNode right;
    private transient RbNode parent;
    private transient int color;

    @Override
    public final void setNode(RbNode left, RbNode right, RbNode parent, int color) {
        this.left = left;
        this.right = right;
        this.parent = parent;
        this.color = color;
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
