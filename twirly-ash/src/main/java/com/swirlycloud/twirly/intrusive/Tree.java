/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

public abstract class Tree<T> {
    private static final int NONE = 0;
    private static final int BLACK = 1;
    private static final int RED = 2;

    private T root;

    protected abstract T setLeft(T node, T left);

    protected abstract T setRight(T node, T right);

    protected abstract T setParent(T node, T parent);

    protected abstract void setColor(T node, int color);

    protected abstract T next(T node);

    protected abstract T prev(T node);

    protected abstract long getKey(T node);

    protected abstract T getLeft(T node);

    protected abstract T getRight(T node);

    protected abstract T getParent(T node);

    protected abstract int getColor(T node);

    private static int cmp(long lhs, long rhs) {
        int i;
        if (lhs < rhs) {
            i = -1;
        } else if (lhs > rhs) {
            i = 1;
        } else {
            i = 0;
        }
        return i;
    }

    private final void set(T node, T parent) {
        setLeft(node, setRight(node, null));
        setParent(node, parent);
        setColor(node, RED);
    }

    private final void setBlackRed(T black, T red) {
        setColor(black, BLACK);
        setColor(red, RED);
    }

    private final void rotateLeft(T node, T tmp) {
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

    private final void rotateRight(T node, T tmp) {
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

    private final void insertColor(T node) {
        T parent, gparent, tmp;
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

    private final void removeColor(T parent, T node) {
        T tmp;
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
                        T oleft;
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
                        T oright;
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

    public final T insert(T node) {
        assert getColor(node) == NONE;
        T tmp;
        T parent = null;
        int comp = 0;
        tmp = root;
        while (tmp != null) {
            parent = tmp;
            comp = cmp(getKey(node), getKey(parent));
            if (comp < 0) {
                tmp = getLeft(tmp);
            } else if (comp > 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        set(node, parent);
        if (parent != null) {
            if (comp < 0) {
                setLeft(parent, node);
            } else {
                setRight(parent, node);
            }
        } else {
            root = node;
        }
        insertColor(node);
        return node;
    }

    public final void pinsert(T node, T parent) {
        assert getColor(node) == NONE;
        set(node, parent);
        if (parent != null) {
            final int comp = cmp(getKey(node), getKey(parent));
            if (comp < 0) {
                setLeft(parent, node);
            } else {
                setRight(parent, node);
            }
        } else {
            root = node;
        }
        insertColor(node);
    }

    public final T remove(T node) {
        T child, parent;
        final T old = node;
        int color;
        if (getLeft(node) == null) {
            child = getRight(node);
        } else if (getRight(node) == null) {
            child = getLeft(node);
        } else {
            T left;
            node = getRight(node);
            while ((left = getLeft(node)) != null) {
                node = left;
            }
            child = getRight(node);
            parent = getParent(node);
            color = getColor(node);
            if (child != null) {
                setParent(child, parent);
            }
            if (parent != null) {
                if (getLeft(parent) == node) {
                    setLeft(parent, child);
                } else {
                    setRight(parent, child);
                }
            } else {
                root = child;
            }
            if (getParent(node) == old) {
                parent = node;
            }

            setLeft(node, getLeft(old));
            setRight(node, getRight(old));
            setParent(node, getParent(old));
            setColor(node, getColor(old));

            if (getParent(old) != null) {
                if (getLeft(getParent(old)) == old) {
                    setLeft(getParent(old), node);
                } else {
                    setRight(getParent(old), node);
                }
            } else {
                root = node;
            }
            setParent(getLeft(old), node);
            if (getRight(old) != null) {
                setParent(getRight(old), node);
            }
            if (parent != null) {
                left = parent;
            }
            if (color == BLACK) {
                removeColor(parent, child);
            }
            setColor(old, NONE);
            return old;
        }
        parent = getParent(node);
        color = getColor(node);
        if (child != null) {
            setParent(child, parent);
        }
        if (parent != null) {
            if (getLeft(parent) == node) {
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
        setColor(old, NONE);
        return old;
    }

    /**
     * Finds the node with the same key as node.
     */

    public final T find(long key) {
        T tmp = root;
        int comp;
        while (tmp != null) {
            comp = cmp(key, getKey(tmp));
            if (comp < 0) {
                tmp = getLeft(tmp);
            } else if (comp > 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Finds the first node greater than or equal to the search key.
     */

    public final T nfind(long key) {
        T tmp = root;
        T res = null;
        int comp;
        while (tmp != null) {
            comp = cmp(key, getKey(tmp));
            if (comp < 0) {
                res = tmp;
                tmp = getLeft(tmp);
            } else if (comp > 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return res;
    }

    // Extensions.

    /**
     * Return match or parent.
     */

    public final T pfind(long key) {
        T tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = cmp(key, getKey(tmp));
            if (comp < 0) {
                tmp = getLeft(tmp);
            } else if (comp > 0) {
                tmp = getRight(tmp);
            } else {
                return tmp;
            }
        }
        return parent;
    }

    /**
     * If you want fast access to any node, then root is your best choice.
     * 
     * @return the root node.
     */
    public final T getRoot() {
        return root;
    }

    public final T getFirst() {
        T tmp = root;
        T parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = getLeft(tmp);
        }
        return parent;
    }

    public final T getLast() {
        T tmp = root;
        T parent = null;
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
