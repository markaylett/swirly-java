/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * 
 * @author Mark Aylett
 */
public abstract @NonNullByDefault class Tree<V> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int INDENT = 4;
    private static final int NONE = 0;
    private static final int BLACK = 1;
    private static final int RED = 2;

    private final void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private final void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    protected @Nullable V root;

    protected abstract void setNode(V lhs, V rhs);

    protected abstract void setLeft(V node, @Nullable V left);

    protected abstract void setRight(V node, @Nullable V right);

    protected abstract void setParent(V node, @Nullable V parent);

    protected abstract void setColor(V node, int color);

    protected abstract @Nullable V next(V node);

    protected abstract @Nullable V prev(V node);

    protected abstract @Nullable V getLeft(V node);

    protected abstract @Nullable V getRight(V node);

    protected abstract @Nullable V getParent(V node);

    protected abstract int getColor(V node);

    protected abstract int compareKey(V lhs, V rhs);

    private final void print(final PrintStream out, final @Nullable V node, int indent) {
        if (node == null) {
            out.print("<empty>");
            return;
        }
        final V right = getRight(node);
        if (right != null) {
            print(out, right, indent + INDENT);
        }
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        if (getColor(node) == BLACK) {
            out.println(node + ":B");
        } else {
            out.println(node + ":R");
        }
        final V left = getLeft(node);
        if (left != null) {
            print(out, left, indent + INDENT);
        }
    }

    private final V getSucc(final V node) {
        V succ = getRight(node);
        assert succ != null;
        for (;;) {
            final V left = getLeft(succ);
            if (left == null) {
                break;
            }
            succ = left;
        }
        return succ;
    }

    private final void set(final V node, final @Nullable V parent) {
        setLeft(node, null);
        setRight(node, null);
        setParent(node, parent);
        setColor(node, RED);
    }

    private final void setBlackRed(final V black, final V red) {
        setColor(black, BLACK);
        setColor(red, RED);
    }

    private final boolean isNullOrBlack(final @Nullable V node) {
        return node == null || getColor(node) == BLACK;
    }

    private final @Nullable V replaceChildOfParent(final V old, final @Nullable V node) {
        final V parent = getParent(old);
        if (parent != null) {
            if (getLeft(parent) == old) {
                setLeft(parent, node);
            } else {
                assert getRight(parent) == old;
                setRight(parent, node);
            }
        } else {
            root = node;
        }
        return parent;
    }

    private final void replaceParent(final V old, final @Nullable V node) {
        final V parent = replaceChildOfParent(old, node);
        if (node != null) {
            setParent(node, parent);
        }
    }

    private final void replace(final V old, final V node) {
        final V left = getLeft(old);
        setLeft(node, left);
        if (left != null) {
            setParent(left, node);
        }
        final V right = getRight(old);
        setRight(node, right);
        if (right != null) {
            setParent(right, node);
        }
        replaceParent(old, node);
        setColor(node, getColor(old));
    }

    private final void rotateLeft(final V node) {
        final V right = getRight(node);
        assert right != null;
        replaceParent(node, right);
        final V left = getLeft(right);
        setRight(node, left);
        if (left != null) {
            assert left != null;
            setParent(left, node);
        }
        setLeft(right, node);
        setParent(node, right);
    }

    private final void rotateRight(final V node) {
        final V left = getLeft(node);
        assert left != null;
        replaceParent(node, left);
        final V right = getRight(left);
        setLeft(node, right);
        if (right != null) {
            setParent(right, node);
        }
        setRight(left, node);
        setParent(node, left);
    }

    private final void insertColor(V node) {
        V parent, gparent, tmp;
        while ((parent = getParent(node)) != null && getColor(parent) == RED) {
            gparent = getParent(parent);
            assert gparent != null;
            if (getLeft(gparent) == parent) {
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
                assert parent != null;
                setBlackRed(parent, gparent);
                rotateRight(gparent);
            } else {
                assert getRight(gparent) == parent;
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
                assert parent != null;
                setBlackRed(parent, gparent);
                rotateLeft(gparent);
            }
        }
        final V root = this.root;
        assert root != null;
        setColor(root, BLACK);
    }

    private final void removeColor(@Nullable V parent, @Nullable V node) {
        V tmp;
        while (isNullOrBlack(node) && node != root) {
            assert parent != null;
            if (getLeft(parent) == node) {
                tmp = getRight(parent);
                assert tmp != null;
                if (getColor(tmp) == RED) {
                    setBlackRed(tmp, parent);
                    rotateLeft(parent);
                    tmp = getRight(parent);
                }
                assert tmp != null;
                if (isNullOrBlack(getLeft(tmp)) && isNullOrBlack(getRight(tmp))) {
                    setColor(tmp, RED);
                    node = parent;
                    assert node != null;
                    parent = getParent(node);
                } else {
                    if (isNullOrBlack(getRight(tmp))) {
                        V oleft;
                        if ((oleft = getLeft(tmp)) != null) {
                            setColor(oleft, BLACK);
                        }
                        setColor(tmp, RED);
                        rotateRight(tmp);
                        tmp = getRight(parent);
                    }
                    assert tmp != null;
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    final V right = getRight(tmp);
                    if (right != null) {
                        setColor(right, BLACK);
                    }
                    rotateLeft(parent);
                    node = root;
                    break;
                }
            } else {
                assert getRight(parent) == node;
                tmp = getLeft(parent);
                assert tmp != null;
                if (getColor(tmp) == RED) {
                    setBlackRed(tmp, parent);
                    rotateRight(parent);
                    tmp = getLeft(parent);
                }
                assert tmp != null;
                if (isNullOrBlack(getLeft(tmp)) && isNullOrBlack(getRight(tmp))) {
                    setColor(tmp, RED);
                    node = parent;
                    assert node != null;
                    parent = getParent(node);
                } else {
                    if (isNullOrBlack(getLeft(tmp))) {
                        V oright;
                        if ((oright = getRight(tmp)) != null) {
                            setColor(oright, BLACK);
                        }
                        setColor(tmp, RED);
                        rotateLeft(tmp);
                        tmp = getLeft(parent);
                    }
                    assert tmp != null;
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    final V left = getLeft(tmp);
                    if (left != null) {
                        setColor(left, BLACK);
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

    public Tree() {
        clear();
    }

    public final void print(final PrintStream out) {
        print(out, root, 0);
    }

    public final void print() {
        final PrintStream out = System.out;
        assert out != null;
        print(out);
    }

    public final void clear() {
        root = null;
    }

    /**
     * Inserts a node into the RB tree.
     */
    public final @Nullable V insert(final V node) {
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
                replace(tmp, node);
                return tmp;
            }
        }
        pinsert(node, parent);
        return null;
    }

    public final void pinsert(final V node, final @Nullable V parent) {
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
        int color;

        final V left = getLeft(node);
        V right = getRight(node);
        if (left == null) {
            child = right;
        } else if (right == null) {
            child = left;
        } else {
            assert left != null;
            assert right != null;
            final V succ = getSucc(node);
            child = getRight(succ);
            parent = getParent(succ);
            color = getColor(succ);

            replaceParent(succ, child);

            if (parent == node) {
                parent = succ;
            }

            setNode(succ, node);

            replaceChildOfParent(node, succ);
            setParent(left, succ);

            right = getRight(node);
            if (right != null) {
                setParent(right, succ);
            }
            if (color == BLACK) {
                assert parent != null;
                removeColor(parent, child);
            }
            setColor(node, NONE);
            return node;
        }
        parent = getParent(node);
        color = getColor(node);

        replaceParent(node, child);

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
    public final @Nullable V getRoot() {
        return root;
    }

    public final @Nullable V getFirst() {
        V tmp = root;
        V parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = getLeft(tmp);
        }
        return parent;
    }

    public final @Nullable V getLast() {
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
