/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

public final class Tree {
    private static final int NONE = 0;
    private static final int BLACK = 1;
    private static final int RED = 2;

    private RbNode root;

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

    private static void set(RbNode node, RbNode parent) {
        node.setLeft(node.setRight(null));
        node.setParent(parent);
        node.setColor(RED);
    }

    private static void setBlackRed(RbNode black, RbNode red) {
        black.setColor(BLACK);
        red.setColor(RED);
    }

    private final void rotateLeft(RbNode node, RbNode tmp) {
        tmp = node.getRight();
        if ((node.setRight(tmp.getLeft())) != null) {
            tmp.getLeft().setParent(node);
        }
        if ((tmp.setParent(node.getParent())) != null) {
            if (node == node.getParent().getLeft()) {
                node.getParent().setLeft(tmp);
            } else {
                node.getParent().setRight(tmp);
            }
        } else {
            root = tmp;
        }
        tmp.setLeft(node);
        node.setParent(tmp);
    }

    private final void rotateRight(RbNode node, RbNode tmp) {
        tmp = node.getLeft();
        if ((node.setLeft(tmp.getRight())) != null) {
            tmp.getRight().setParent(node);
        }
        if ((tmp.setParent(node.getParent())) != null) {
            if (node == node.getParent().getLeft()) {
                node.getParent().setLeft(tmp);
            } else {
                node.getParent().setRight(tmp);
            }
        } else {
            root = tmp;
        }
        tmp.setRight(node);
        node.setParent(tmp);
    }

    private final void insertColor(RbNode node) {
        RbNode parent, gparent, tmp;
        while ((parent = node.getParent()) != null && parent.getColor() == RED) {
            gparent = parent.getParent();
            if (parent == gparent.getLeft()) {
                tmp = gparent.getRight();
                if (tmp != null && tmp.getColor() == RED) {
                    tmp.setColor(BLACK);
                    setBlackRed(parent, gparent);
                    node = gparent;
                    continue;
                }
                if (parent.getRight() == node) {
                    rotateLeft(parent, tmp);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateRight(gparent, tmp);
            } else {
                tmp = gparent.getLeft();
                if (tmp != null && tmp.getColor() == RED) {
                    tmp.setColor(BLACK);
                    setBlackRed(parent, gparent);
                    node = gparent;
                    continue;
                }
                if (parent.getLeft() == node) {
                    rotateRight(parent, tmp);
                    tmp = parent;
                    parent = node;
                    node = tmp;
                }
                setBlackRed(parent, gparent);
                rotateLeft(gparent, tmp);
            }
        }
        root.setColor(BLACK);
    }

    private final void removeColor(RbNode parent, RbNode node) {
        RbNode tmp;
        while ((node == null || node.getColor() == BLACK) && node != root) {
            if (parent.getLeft() == node) {
                tmp = parent.getRight();
                if (tmp.getColor() == RED) {
                    setBlackRed(tmp, parent);
                    rotateLeft(parent, tmp);
                    tmp = parent.getRight();
                }
                if ((tmp.getLeft() == null || tmp.getLeft().getColor() == BLACK)
                        && (tmp.getRight() == null || tmp.getRight().getColor() == BLACK)) {
                    tmp.setColor(RED);
                    node = parent;
                    parent = node.getParent();
                } else {
                    if (tmp.getRight() == null || tmp.getRight().getColor() == BLACK) {
                        RbNode oleft;
                        if ((oleft = tmp.getLeft()) != null) {
                            oleft.setColor(BLACK);
                        }
                        tmp.setColor(RED);
                        rotateRight(tmp, oleft);
                        tmp = parent.getRight();
                    }
                    tmp.setColor(parent.getColor());
                    parent.setColor(BLACK);
                    if (tmp.getRight() != null) {
                        tmp.getRight().setColor(BLACK);
                    }
                    rotateLeft(parent, tmp);
                    node = root;
                    break;
                }
            } else {
                tmp = parent.getLeft();
                if (tmp.getColor() == RED) {
                    setBlackRed(tmp, parent);
                    rotateRight(parent, tmp);
                    tmp = parent.getLeft();
                }
                if ((tmp.getLeft() == null || tmp.getLeft().getColor() == BLACK)
                        && (tmp.getRight() == null || tmp.getRight().getColor() == BLACK)) {
                    tmp.setColor(RED);
                    node = parent;
                    parent = node.getParent();
                } else {
                    if (tmp.getLeft() == null || tmp.getLeft().getColor() == BLACK) {
                        RbNode oright;
                        if ((oright = tmp.getRight()) != null) {
                            oright.setColor(BLACK);
                        }
                        tmp.setColor(RED);
                        rotateLeft(tmp, oright);
                        tmp = parent.getLeft();
                    }
                    tmp.setColor(parent.getColor());
                    parent.setColor(BLACK);
                    if (tmp.getLeft() != null) {
                        tmp.getLeft().setColor(BLACK);
                    }
                    rotateRight(parent, tmp);
                    node = root;
                    break;
                }
            }
        }
        if (node != null) {
            node.setColor(BLACK);
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

    public final RbNode insert(RbNode node) {
        assert node.getColor() == NONE;
        RbNode tmp;
        RbNode parent = null;
        int comp = 0;
        tmp = root;
        while (tmp != null) {
            parent = tmp;
            comp = cmp(node.getKey(), parent.getKey());
            if (comp < 0) {
                tmp = tmp.getLeft();
            } else if (comp > 0) {
                tmp = tmp.getRight();
            } else {
                return tmp;
            }
        }
        set(node, parent);
        if (parent != null) {
            if (comp < 0) {
                parent.setLeft(node);
            } else {
                parent.setRight(node);
            }
        } else {
            root = node;
        }
        insertColor(node);
        return node;
    }

    public final void pinsert(RbNode node, RbNode parent) {
        assert node.getColor() == NONE;
        set(node, parent);
        if (parent != null) {
            final int comp = cmp(node.getKey(), parent.getKey());
            if (comp < 0) {
                parent.setLeft(node);
            } else {
                parent.setRight(node);
            }
        } else {
            root = node;
        }
        insertColor(node);
    }

    public final RbNode remove(RbNode node) {
        RbNode child, parent;
        final RbNode old = node;
        int color;
        if (node.getLeft() == null) {
            child = node.getRight();
        } else if (node.getRight() == null) {
            child = node.getLeft();
        } else {
            RbNode left;
            node = node.getRight();
            while ((left = node.getLeft()) != null) {
                node = left;
            }
            child = node.getRight();
            parent = node.getParent();
            color = node.getColor();
            if (child != null) {
                child.setParent(parent);
            }
            if (parent != null) {
                if (parent.getLeft() == node) {
                    parent.setLeft(child);
                } else {
                    parent.setRight(child);
                }
            } else {
                root = child;
            }
            if (node.getParent() == old) {
                parent = node;
            }

            node.setLeft(old.getLeft());
            node.setRight(old.getRight());
            node.setParent(old.getParent());
            node.setColor(old.getColor());

            if (old.getParent() != null) {
                if (old.getParent().getLeft() == old) {
                    old.getParent().setLeft(node);
                } else {
                    old.getParent().setRight(node);
                }
            } else {
                root = node;
            }
            old.getLeft().setParent(node);
            if (old.getRight() != null) {
                old.getRight().setParent(node);
            }
            if (parent != null) {
                left = parent;
            }
            if (color == BLACK) {
                removeColor(parent, child);
            }
            old.setColor(NONE);
            return old;
        }
        parent = node.getParent();
        color = node.getColor();
        if (child != null) {
            child.setParent(parent);
        }
        if (parent != null) {
            if (parent.getLeft() == node) {
                parent.setLeft(child);
            } else {
                parent.setRight(child);
            }
        } else {
            root = child;
        }
        if (color == BLACK) {
            removeColor(parent, child);
        }
        old.setColor(NONE);
        return old;
    }

    /**
     * Finds the node with the same key as node.
     */

    public final RbNode find(long key) {
        RbNode tmp = root;
        int comp;
        while (tmp != null) {
            comp = cmp(key, tmp.getKey());
            if (comp < 0) {
                tmp = tmp.getLeft();
            } else if (comp > 0) {
                tmp = tmp.getRight();
            } else {
                return tmp;
            }
        }
        return null;
    }

    /**
     * Finds the first node greater than or equal to the search key.
     */

    public final RbNode nfind(long key) {
        RbNode tmp = root;
        RbNode res = null;
        int comp;
        while (tmp != null) {
            comp = cmp(key, tmp.getKey());
            if (comp < 0) {
                res = tmp;
                tmp = tmp.getLeft();
            } else if (comp > 0) {
                tmp = tmp.getRight();
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

    public final RbNode pfind(long key) {
        RbNode tmp = root, parent = null;
        while (tmp != null) {
            parent = tmp;
            final int comp = cmp(key, tmp.getKey());
            if (comp < 0) {
                tmp = tmp.getLeft();
            } else if (comp > 0) {
                tmp = tmp.getRight();
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
    public final RbNode getRoot() {
        return root;
    }

    public final RbNode getFirst() {
        RbNode tmp = root;
        RbNode parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = tmp.getLeft();
        }
        return parent;
    }

    public final RbNode getLast() {
        RbNode tmp = root;
        RbNode parent = null;
        while (tmp != null) {
            parent = tmp;
            tmp = tmp.getRight();
        }
        return parent;
    }

    public final boolean isEmpty() {
        return root == null;
    }
}
