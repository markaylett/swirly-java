/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import static com.swirlycloud.twirly.util.CollectionUtil.compareInt;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.intrusive.InstructTree;
import com.swirlycloud.twirly.intrusive.RefHashTable;
import com.swirlycloud.twirly.intrusive.Tree;
import com.swirlycloud.twirly.node.BasicRbNode;
import com.swirlycloud.twirly.node.RbNode;

public final class Sess extends BasicRbNode {

    public final class PosnTree extends Tree<RbNode> {

        @Override
        protected final int compareKey(RbNode lhs, RbNode rhs) {
            final Posn r = (Posn) rhs;
            return compareKey(lhs, r.getContr(), r.getSettlDay());
        }

        protected final int compareKey(RbNode lhs, String contr, int settlDay) {
            final Posn l = (Posn) lhs;
            int n = l.getContr().compareTo(contr);
            if (n == 0) {
                n = compareInt(l.getSettlDay(), settlDay);
            }
            return n;
        }

        public final RbNode find(String contr, int settlDay) {
            RbNode tmp = root;
            int comp;
            while (tmp != null) {
                comp = compareKey(tmp, contr, settlDay);
                if (comp > 0) {
                    tmp = getLeft(tmp);
                } else if (comp < 0) {
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

        public final RbNode nfind(String contr, int settlDay) {
            RbNode tmp = root;
            RbNode res = null;
            int comp;
            while (tmp != null) {
                comp = compareKey(tmp, contr, settlDay);
                if (comp > 0) {
                    res = tmp;
                    tmp = getLeft(tmp);
                } else if (comp < 0) {
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

        public final RbNode pfind(String contr, int settlDay) {
            RbNode tmp = root, parent = null;
            while (tmp != null) {
                parent = tmp;
                final int comp = compareKey(tmp, contr, settlDay);
                if (comp > 0) {
                    tmp = getLeft(tmp);
                } else if (comp < 0) {
                    tmp = getRight(tmp);
                } else {
                    return tmp;
                }
            }
            return parent;
        }

        @Override
        protected final void setNode(RbNode node, RbNode left, RbNode right, RbNode parent,
                int color) {
            node.setNode(left, right, parent, color);
        }

        @Override
        protected final RbNode setLeft(RbNode node, RbNode left) {
            return node.setLeft(left);
        }

        @Override
        protected final RbNode setRight(RbNode node, RbNode right) {
            return node.setRight(right);
        }

        @Override
        protected final RbNode setParent(RbNode node, RbNode parent) {
            return node.setParent(parent);
        }

        @Override
        protected final void setColor(RbNode node, int color) {
            node.setColor(color);
        }

        @Override
        protected final RbNode next(RbNode node) {
            return node.rbNext();
        }

        @Override
        protected final RbNode prev(RbNode node) {
            return node.rbPrev();
        }

        @Override
        protected final RbNode getLeft(RbNode node) {
            return node.getLeft();
        }

        @Override
        protected final RbNode getRight(RbNode node) {
            return node.getRight();
        }

        @Override
        protected final RbNode getParent(RbNode node) {
            return node.getParent();
        }

        @Override
        protected final int getColor(RbNode node) {
            return node.getColor();
        }
    }

    private final Trader trader;
    private final RefHashTable refIdx;
    private final InstructTree orders = new InstructTree();
    private final InstructTree trades = new InstructTree();
    private final PosnTree posns = new PosnTree();

    public Sess(Trader trader, RefHashTable refIdx) {
        this.trader = trader;
        this.refIdx = refIdx;
    }

    public final String getTrader() {
        return trader.getMnem();
    }

    public final Trader getTraderRich() {
        return trader;
    }

    final void insertOrder(Order order) {
        final RbNode unused = orders.insert(order);
        assert unused == null;
        if (order.getRef() != null) {
            refIdx.insert(order);
        }
    }

    final void removeOrder(Order order) {
        assert order.getTrader().equals(trader.getMnem());
        orders.remove(order);
        if (order.getRef() != null) {
            refIdx.remove(trader.getMnem(), order.getRef());
        }
    }

    final Order removeOrder(String market, long id) {
        final RbNode node = orders.find(market, id);
        if (node == null) {
            return null;
        }
        final Order order = (Order) node;
        removeOrder(order);
        return order;
    }

    final Order removeOrder(String ref) {
        final Order order = (Order) refIdx.remove(trader.getMnem(), ref);
        if (order != null) {
            orders.remove(order);
        }
        return order;
    }

    public final Order findOrder(String market, long id) {
        return (Order) orders.find(market, id);
    }

    public final Order findOrder(String ref) {
        assert ref != null;
        return (Order) refIdx.find(trader.getMnem(), ref);
    }

    public final RbNode getRootOrder() {
        return orders.getRoot();
    }

    public final RbNode getFirstOrder() {
        return orders.getFirst();
    }

    public final RbNode getLastOrder() {
        return orders.getLast();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    final void insertTrade(Exec trade) {
        final RbNode unused = trades.insert(trade);
        assert unused == null;
    }

    final void removeTrade(Exec trade) {
        trades.remove(trade);
    }

    final boolean removeTrade(String market, long id) {
        final RbNode node = trades.find(market, id);
        if (node == null) {
            return false;
        }
        trades.remove(node);
        return true;
    }

    public final Exec findTrade(String market, long id) {
        return (Exec) trades.find(market, id);
    }

    public final RbNode getRootTrade() {
        return trades.getRoot();
    }

    public final RbNode getFirstTrade() {
        return trades.getFirst();
    }

    public final RbNode getLastTrade() {
        return trades.getLast();
    }

    public final boolean isEmptyTrade() {
        return trades.isEmpty();
    }

    final void insertPosn(Posn posn) {
        final RbNode unused = posns.insert(posn);
        assert unused == null;
    }

    final Posn updatePosn(Posn posn) {
        final String contr = posn.getContr();
        final int settlDay = posn.getSettlDay();
        final Posn exist = (Posn) posns.pfind(contr, settlDay);
        if (exist != null && exist.getContr().equals(contr) && exist.getSettlDay() == settlDay) {

            // Update existing position.

            exist.setBuyCost(posn.getBuyCost());
            exist.setBuyLots(posn.getBuyLots());
            exist.setSellCost(posn.getSellCost());
            exist.setSellLots(posn.getSellLots());

            posn = exist;
        } else {
            final RbNode parent = exist;
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    final Posn getLazyPosn(Market market) {
        Posn posn = (Posn) posns.pfind(market.getContr(), market.getSettlDay());
        if (posn == null || !posn.getContr().equals(market.getContr())
                || posn.getSettlDay() != market.getSettlDay()) {
            final RbNode parent = posn;
            posn = new Posn(trader.getMnem(), market.getContr(), market.getSettlDay());
            posns.pinsert(posn, parent);
        }
        return posn;
    }

    public final Posn findPosn(String contr, int settlDay) {
        return (Posn) posns.find(contr, settlDay);
    }

    public final RbNode getRootPosn() {
        return posns.getRoot();
    }

    public final RbNode getFirstPosn() {
        return posns.getFirst();
    }

    public final RbNode getLastPosn() {
        return posns.getLast();
    }

    public final boolean isEmptyPosn() {
        return posns.isEmpty();
    }
}
