/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Tree<V> {
    private static final int NONE = 0;
    private static final int BLACK = 1;
    private static final int RED = 2;

    protected V root;

    protected abstract void setNode(V node, V left, V right, V parent, int color);

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

    private final void set(V node, V parent) {
        setLeft(node, setRight(node, null));
        setParent(node, parent);
        setColor(node, RED);
    }

    private final void setBlackRed(V black, V red) {
        setColor(black, BLACK);
        setColor(red, RED);
    }

    private final void rotateLeft(V node, V tmp) {
        tmp = getRight(node);
        if ((setRight(node, getLeft(tmp))) != null) {
            setParent(getLeft(tmp), node);
        }
        if ((setParent(tmp, getParent(node))) != null) {
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

    private final void rotateRight(V node, V tmp) {
        tmp = getLeft(node);
        if ((setLeft(node, getRight(tmp))) != null) {
            setParent(getRight(tmp), node);
        }
        if ((setParent(tmp, getParent(node))) != null) {
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
                    rotateLeft(parent, tmp);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateRight(gparent, tmp);
            } else {
                tmp = getLeft(gparent);
                if (tmp != null && getColor(tmp) == RED) {
                    setColor(tmp, BLACK);
                    setBlackRed(parent, gparent);
                    node = gparent;
                    continue;
                }
                if (getLeft(parent) == node) {
                    rotateRight(parent, tmp);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateLeft(gparent, tmp);
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
                    rotateLeft(parent, tmp);
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
                        rotateRight(tmp, oleft);
                        tmp = getRight(parent);
                    }
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    if (getRight(tmp) != null) {
                        setColor(getRight(tmp), BLACK);
                    }
                    rotateLeft(parent, tmp);
                    node = root;
                    break;
                }
            } else {
                tmp = getLeft(parent);
                if (getColor(tmp) == RED) {
                    setBlackRed(tmp, parent);
                    rotateRight(parent, tmp);
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
                        rotateLeft(tmp, oright);
                        tmp = getLeft(parent);
                    }
                    setColor(tmp, getColor(parent));
                    setColor(parent, BLACK);
                    if (getLeft(tmp) != null) {
                        setColor(getLeft(tmp), BLACK);
                    }
                    rotateRight(parent, tmp);
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
                setNode(node, getLeft(tmp), getRight(tmp), getParent(tmp), getColor(tmp));
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

            setNode(tmp, getLeft(node), getRight(node), getParent(node), getColor(node));

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
