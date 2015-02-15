/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public final class RbUtil {
    private RbUtil() {
    }

    public static RbNode rbNext(RbNode node) {
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

    public static RbNode rbPrev(RbNode node) {
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
}
