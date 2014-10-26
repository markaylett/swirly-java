/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.util;

public abstract class BasicRbNode implements RbNode {

    private transient RbNode left;
    private transient RbNode right;
    private transient RbNode parent;
    private transient int color;

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
        RbNode node = this;
        if (node.getRight() != null) {
            node = node.getRight();
            while (node.getLeft() != null) {
                node = node.getLeft();
            }
        } else {
            if (node.getParent() != null && node == node.getParent().getLeft()) {
                node = node.getParent();
            } else {
                while (node.getParent() != null && node == node.getParent().getRight()) {
                    node = node.getParent();
                }
                node = node.getParent();
            }
        }
        return node;
    }

    @Override
    public final RbNode rbPrev() {
        RbNode node = this;
        if (node.getLeft() != null) {
            node = node.getLeft();
            while (node.getRight() != null) {
                node = node.getRight();
            }
        } else {
            if (node.getParent() != null && node == node.getParent().getRight()) {
                node = node.getParent();
            } else {
                while (node.getParent() != null && node == node.getParent().getLeft()) {
                    node = node.getParent();
                }
                node = node.getParent();
            }
        }
        return node;
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
