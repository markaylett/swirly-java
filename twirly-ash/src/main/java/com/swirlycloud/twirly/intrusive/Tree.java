/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import java.io.PrintStream;

import org.eclipse.jdt.annotation.Nullable;

/**
 * In addition to the requirements imposed on a binary search tree the following must be satisfied
 * by a red-black tree:
 * <ol>
 * <li>A node is either red or black.</li>
 * <li>The root is black. (This rule is sometimes omitted. Since the root can always be changed from
 * red to black, but not necessarily vice versa, this rule has little effect on analysis).</li>
 * <li>All leaves (NIL) are black. All leaves are of the same color as the root.</li>
 * <li>Every red node must have two black child nodes, and therefore it must have a black parent.</li>
 * <li>Every path from a given node to any of its descendant NIL nodes contains the same number of
 * black nodes.</li>
 * </ol>
 */
public abstract class Tree<V> {
    private static final int INDENT = 4;
    private static final int NONE = 0;
    private static final int BLACK = 1;
    private static final int RED = 2;

    protected V root;

    protected abstract void setNode(V lhs, V rhs);

    protected abstract V setLeft(V node, V left);

    protected abstract V setRight(V node, V right);

    protected abstract V setParent(V node, V parent);

    protected abstract void setColor(V node, int color);

    protected abstract V next(V node);

    protected abstract V prev(V node);

    protected abstract V getLeft(V node);

    protected abstract V getRight(V node);

    protected abstract V getParent(V node);

    protected abstract int getColor(V node);

    protected abstract int compareKey(V lhs, V rhs);

    private final void print(PrintStream out, final V n, int indent) {
        if (n == null) {
            out.print("<empty>");
            return;
        }
        if (getRight(n) != null) {
            print(out, getRight(n), indent + INDENT);
        }
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        if (getColor(n) == BLACK) {
            out.println(n + ":B");
        } else {
            out.println(n + ":R");
        }
        if (getLeft(n) != null) {
            print(out, getLeft(n), indent + INDENT);
        }
    }

    private final V setLeftAndGet(V node, V left) {
        setLeft(node, left);
        return left;
    }

    private final V setRightAndGet(V node, V right) {
        setRight(node, right);
        return right;
    }

    private final V setParentAndGet(V node, V parent) {
        setParent(node, parent);
        return parent;
    }

    private final void set(V node, V parent) {
        setLeft(node, null);
        setRight(node, null);
        setParent(node, parent);
        setColor(node, RED);
    }

    private final void setBlackRed(V black, V red) {
        setColor(black, BLACK);
        setColor(red, RED);
    }

    private final void rotateLeft(V node) {
        final V tmp = getRight(node);
        if ((setRightAndGet(node, getLeft(tmp))) != null) {
            setParent(getLeft(tmp), node);
        }
        if ((setParentAndGet(tmp, getParent(node))) != null) {
            if (node == getLeft(getParent(node))) {
                setLeft(getParent(node), tmp);
            } else {
                setRight(getParent(node), tmp);
            }
        } else {
            root = tmp;
        }
        setLeft(tmp, node);
        setParent(node, tmp);
    }

    private final void rotateRight(V node) {
        final V tmp = getLeft(node);
        if ((setLeftAndGet(node, getRight(tmp))) != null) {
            setParent(getRight(tmp), node);
        }
        if ((setParentAndGet(tmp, getParent(node))) != null) {
            if (node == getLeft(getParent(node))) {
                setLeft(getParent(node), tmp);
            } else {
                setRight(getParent(node), tmp);
            }
        } else {
            root = tmp;
        }
        setRight(tmp, node);
        setParent(node, tmp);
    }

    private final void insertColor(V node) {
        V parent, gparent, tmp;
        while ((parent = getParent(node)) != null && getColor(parent) == RED) {
            gparent = getParent(parent);
            if (parent == getLeft(gparent)) {
                tmp = getRight(gparent);
                if (tmp != null && getColor(tmp) == RED) {
                    setColor(tmp, BLACK);
                    setBlackRed(parent, gparent);
                    node = gparent;
                    continue;
                }
                if (getRight(parent) == node) {
                    rotateLeft(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateRight(gparent);
            } else {
                tmp = getLeft(gparent);
                if (tmp != null && getColor(tmp) == RED) {
                    setColor(tmp, BLACK);
                    setBlackRed(parent, gparent);
                    node = gparent;
                    continue;
                }
                if (getLeft(parent) == node) {
                    rotateRight(parent);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateLeft(gparent);
            }
        }
        setColor(root, BLACK);
    }

    private final void removeColor(V parent, V node) {
        V tmp;
        while ((node == null || getColor(node) == BLACK) && node != root) {
            if (getLeft(parent) == node) {
                tmp = getRight(parent);
                if (getColor(tmp) == RED) {
                    setBlackRed(tmp, parent);
                    rotateLeft(parent);
                    tmp = getRight(parent);
                }
                if ((getLeft(tmp) == null || getColor(getLeft(tmp)) == BLACK)
                        && (getRight(tmp) == null || getColor(getRight(tmp)) == BLACK)) {
                    setColor(tmp, RED);
                    node = parent;
                    parent = getParent(node);
                } else {
                    if (getRight(tmp) == null || getColor(getRight(tmp)) == BLACK) {
                        V oleft;
                        if ((oleft = getLeft(tmp)) != null) {
                            setColor(oleft, BLACK);
                        }
                        setColor(tmp, RED);
                        rotateRight(tmp);
                        tmp = getRight(parent);
                    }
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    if (getRight(tmp) != null) {
                        setColor(getRight(tmp), BLACK);
                    }
                    rotateLeft(parent);
                    node = root;
                    break;
                }
            } else {
                tmp = getLeft(parent);
                if (getColor(tmp) == RED) {
                    setBlackRed(tmp, parent);
                    rotateRight(parent);
                    tmp = getLeft(parent);
                }
                if ((getLeft(tmp) == null || getColor(getLeft(tmp)) == BLACK)
                        && (getRight(tmp) == null || getColor(getRight(tmp)) == BLACK)) {
                    setColor(tmp, RED);
                    node = parent;
                    parent = getParent(node);
                } else {
                    if (getLeft(tmp) == null || getColor(getLeft(tmp)) == BLACK) {
                        V oright;
                        if ((oright = getRight(tmp)) != null) {
                            setColor(oright, BLACK);
                        }
                        setColor(tmp, RED);
                        rotateLeft(tmp);
                        tmp = getLeft(parent);
                    }
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    if (getLeft(tmp) != null) {
                        setColor(getLeft(tmp), BLACK);
                    }
                    rotateRight(parent);
                    node = root;
                    break;
                }
            }
        }
        if (node != null) {
            setColor(node, BLACK);
        }
    }

    private final void replace(final @Nullable V node, final V old) {
        final V ol = setLeftAndGet(node, getLeft(old));
        if (ol != null) {
            setParent(ol, node);
        }
        final V or = setRightAndGet(node, getRight(old));
        if (or != null) {
            setParent(or, node);
        }
        final V op = getParent(old);
        if (op == null) {
            root = node;
        } else {
            if (old == getLeft(op)) {
                setLeft(op, node);
            } else {
                setRight(op, node);
            }
        }
        if (node != null) {
            setParent(node, op);
        }
        setColor(node, getColor(old));
    }

    public Tree() {
        clear();
    }

    public final void print(PrintStream out) {
        print(out, root, 0);
    }

    public final void print() {
        print(System.out);
    }

    public final void clear() {
        root = null;
    }

    /**
     * Inserts a node into the RB tree.
     */
    public final V insert(V node) {
        V tmp;
        V parent = null;
        int comp = 0;
        tmp = root;
        while (tmp != null) {
            parent = tmp;
            comp = compareKey(parent, node);
            if (comp > 0) {
                tmp = getLeft(tmp);
            } else if (comp < 0) {
                tmp = getRight(tmp);
            } else {
                replace(node, tmp);
                return tmp;
            }
        }
        pinsert(node, parent);
        return null;
    }

    public final void pinsert(V node, V parent) {
        set(node, parent);
        if (parent != null) {
            final int comp = compareKey(parent, node);
            if (comp > 0) {
                setLeft(parent, node);
            } else {
                setRight(parent, node);
            }
        } else {
            root = node;
        }
        insertColor(node);
    }

    public final V remove(final V node) {
        V child, parent;
        V tmp = node;
        int color;
        if (getLeft(tmp) == null) {
            child = getRight(tmp);
        } else if (getRight(tmp) == null) {
            child = getLeft(tmp);
        } else {
            V left;
            tmp = getRight(tmp);
            while ((left = getLeft(tmp)) != null) {
                tmp = left;
            }
            child = getRight(tmp);
            parent = getParent(tmp);
            color = getColor(tmp);
            if (child != null) {
                setParent(child, parent);
            }
            if (parent != null) {
                if (getLeft(parent) == tmp) {
                    setLeft(parent, child);
                } else {
                    setRight(parent, child);
                }
            } else {
                root = child;
            }
            if (getParent(tmp) == node) {
                parent = tmp;
            }

            setNode(tmp, node);

            if (getParent(node) != null) {
                if (getLeft(getParent(node)) == node) {
                    setLeft(getParent(node), tmp);
                } else {
                    setRight(getParent(node), tmp);
                }
            } else {
                root = tmp;
            }
            setParent(getLeft(node), tmp);
            if (getRight(node) != null) {
                setParent(getRight(node), tmp);
            }
            if (parent != null) {
                left = parent;
            }
            if (color == BLACK) {
                removeColor(parent, child);
            }
            setColor(node, NONE);
            return node;
        }
        parent = getParent(tmp);
        color = getColor(tmp);
        if (child != null) {
            setParent(child, parent);
        }
        if (parent != null) {
            if (getLeft(parent) == tmp) {
                setLeft(parent, child);
            } else {
                setRight(parent, child);
            }
        } else {
            root = child;
        }
        if (color == BLACK) {
            removeColor(parent, child);
        }
        setColor(node, NONE);
        return node;
    }

    /**
     * If you want fast access to any node, then root is your best choice.
     * 
     * @return the root node.
     */
    public final V getRoot() {
        return root;
    }

    public final V getFirst() {
        V tmp = root;
        V parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = getLeft(tmp);
        }
        return parent;
    }

    public final V getLast() {
        V tmp = root;
        V parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = getRight(tmp);
        }
        return parent;
    }

    public final boolean isEmpty() {
        return root == null;
    }
}
