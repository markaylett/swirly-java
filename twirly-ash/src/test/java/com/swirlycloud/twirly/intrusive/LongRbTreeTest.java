/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import static com.swirlycloud.twirly.util.CollectionUtil.compareLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;

import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.RbNode;

public final class LongRbTreeTest {
    private static final boolean DEBUG = false;

    private static final class Node extends BasicRbNode {
        private final long key;
        private final long value;

        Node(long key, long value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public final String toString() {
            return key + "=>" + value;
        }

        final long getValue() {
            return value;
        }
    }

    private static final @NonNullByDefault class NodeTree extends LongRbTree {

        private static long getKey(RbNode node) {
            return ((Node) node).key;
        }

        @Override
        protected final int compareKey(RbNode lhs, RbNode rhs) {
            return compareLong(getKey(lhs), getKey(rhs));
        }

        @Override
        protected final int compareKeyDirect(RbNode lhs, long rhs) {
            return compareLong(getKey(lhs), rhs);
        }
    }

    private static final int BLACK = 1;
    private static final int RED = 2;

    private static int getColor(final RbNode n) {
        return n != null ? n.getColor() : BLACK;
    }

    /**
     * <li>Every red node must have two black child nodes, and therefore it must have a black
     * parent.</li> <li>
     * Every path from a given node to any of its descendant NIL nodes contains the same number of
     * black nodes.</li> </ol>
     */

    /**
     * A node is either red or black.
     * 
     * @param node
     *            The node.
     */
    private static void verifyProperty1(final RbNode node) {
        assertTrue(getColor(node) == RED || getColor(node) == BLACK);
        if (node == null) {
            return;
        }
        verifyProperty1(node.getLeft());
        verifyProperty1(node.getRight());
    }

    /**
     * The root is black. (This rule is sometimes omitted. Since the root can always be changed from
     * red to black, but not necessarily vice versa, this rule has little effect on analysis).
     * 
     * @param n
     *            The node.
     */
    private static void verifyProperty2(final RbNode root) {
        assertEquals(BLACK, getColor(root));
    }

    /**
     * Every red node must have two black child nodes, and therefore it must have a black parent.
     * 
     * @param node
     *            The node.
     */
    private static void verifyProperty4(final RbNode node) {
        if (getColor(node) == RED) {
            assertEquals(BLACK, getColor(node.getLeft()));
            assertEquals(BLACK, getColor(node.getRight()));
            assertEquals(BLACK, getColor(node.getParent()));
        }
        if (node == null) {
            return;
        }
        verifyProperty4(node.getLeft());
        verifyProperty4(node.getRight());
    }

    /**
     * Every path from a given node to any of its descendant NIL nodes contains the same number of
     * black nodes.
     * 
     * @param node
     *            The node.
     */
    private static void verifyProperty5(final RbNode node) {
        verifyProperty5(node, 0, -1);
    }

    private static int verifyProperty5(final RbNode node, int blackCount, int pathBlackCount) {
        if (getColor(node) == BLACK) {
            blackCount++;
        }
        if (node == null) {
            if (pathBlackCount == -1) {
                pathBlackCount = blackCount;
            } else {
                assertEquals(blackCount, pathBlackCount);
            }
            return pathBlackCount;
        }
        pathBlackCount = verifyProperty5(node.getLeft(), blackCount, pathBlackCount);
        pathBlackCount = verifyProperty5(node.getRight(), blackCount, pathBlackCount);
        return pathBlackCount;
    }

    private static void verifyProperties(final NodeTree tree) {
        final RbNode root = tree.getRoot();
        verifyProperty1(root);
        verifyProperty2(root);
        // Property 3 is implicit: all leaves (NIL) are black. All leaves are of the same color as
        // the root.
        verifyProperty4(root);
        verifyProperty5(root);
    }

    @Test
    public final void testOperations() {
        final LongRbTree t = new NodeTree();
        final Node first = new Node(101, 0);
        final Node second = new Node(102, 0);
        final Node third = new Node(103, 0);
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
        assertEquals(first.getColor(), 0);
        t.insert(first);
        assertNotEquals(first.getColor(), 0);
        assertFalse(t.isEmpty());
        assertSame(first, t.getFirst());
        assertSame(first, t.getLast());
        t.insert(second);
        assertSame(first, t.getFirst());
        assertSame(second, t.getLast());
        t.insert(third);
        assertSame(first, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(first);
        assertEquals(first.getColor(), 0);
        assertSame(second, t.getFirst());
        assertSame(third, t.getLast());
        t.remove(second);
        t.remove(third);
        assertTrue(t.isEmpty());
        assertNull(t.getFirst());
        assertNull(t.getLast());
    }

    @Test
    public final void testProperties() {
        final java.util.Random rand = new java.util.Random();

        final NodeTree tree = new NodeTree();
        if (DEBUG) {
            tree.print();
            System.out.println("--");
        }
        for (int i = 0; i < 50; ++i) {
            for (int j = 0; j < 500; j++) {
                final int key = rand.nextInt(1000);
                final int value = rand.nextInt(1000);

                Node node = new Node(key, value);
                tree.insert(node);
                if (DEBUG) {
                    System.out.println("insert " + node + ":");
                    tree.print();
                    System.out.println("--");
                }
                verifyProperties(tree);

                node = (Node) tree.find(key);
                assertNotNull(node);
                assertEquals(value, node.getValue());
            }
            for (int j = 0; j < 6000; j++) {
                final int key = rand.nextInt(1000);
                final Node node = (Node) tree.find(key);
                if (node != null) {
                    tree.remove(node);
                    if (DEBUG) {
                        System.out.println("remove " + key + ":");
                        tree.print();
                        System.out.println("--");
                    }
                    verifyProperties(tree);
                }
            }
        }
    }
}
