/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

public final class RbUtil {
    private RbUtil() {
    }

    public static @Nullable RbNode rbSucc(final RbNode node) {
        RbNode succ = node.getRight();
        if (succ != null) {
            for (;;) {
                final RbNode left = succ.getLeft();
                if (left == null) {
                    break;
                }
                succ = left;
            }
        }
        return succ;
    }

    public static @Nullable RbNode rbPred(final RbNode node) {
        RbNode pred = node.getLeft();
        if (pred != null) {
            for (;;) {
                final RbNode right = pred.getRight();
                if (right == null) {
                    break;
                }
                pred = right;
            }
        }
        return pred;
    }

    public static RbNode rbNext(@NonNull RbNode node) {
        RbNode tmp = node;
        final RbNode right = tmp.getRight(); 
        if (right != null) {
            tmp = right;
            RbNode left = tmp.getLeft(); 
            while (left != null) {
                tmp = left;
                left = tmp.getLeft();
            }
        } else {
            RbNode parent = tmp.getParent();
            if (parent != null && parent.getLeft() == tmp) {
                tmp = parent;
            } else {
                while (parent != null && parent.getRight() == tmp) {
                    tmp = parent;
                    parent = tmp.getParent();
                }
                tmp = tmp.getParent();
            }
        }
        return tmp;
    }

    public static RbNode rbPrev(@NonNull RbNode node) {
        RbNode tmp = node;
        final RbNode left = tmp.getLeft(); 
        if (left != null) {
            tmp = left;
            RbNode right = tmp.getRight();
            while (right != null) {
                tmp = right;
                right = tmp.getRight();
            }
        } else {
            RbNode parent = tmp.getParent();
            if (parent != null && parent.getRight() == tmp) {
                tmp = parent;
            } else {
                while (parent != null && parent.getLeft() == tmp) {
                    tmp = parent;
                    parent = tmp.getParent();
                }
                tmp = tmp.getParent();
            }
        }
        return tmp;
    }
}
