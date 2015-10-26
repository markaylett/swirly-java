/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A place where buyers and sellers come together to exchange goods or services.
 * 
 * @author Mark Aylett
 */
public @NonNullByDefault class MarketBook extends Market {

    private static final long serialVersionUID = 1L;

    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    private transient long lastTicks;
    private transient long lastLots;
    private transient long lastTime;
    // Two sides constitute the book.
    private final transient BookSide bidSide = new BookSide();
    private final transient BookSide offerSide = new BookSide();
    private final transient MarketView view;
    private transient long maxOrderId;
    private transient long maxExecId;
    @Nullable
    private transient MarketBook dirtyNext;

    private final BookSide getSide(Side side) {
        return side == Side.BUY ? bidSide : offerSide;
    }

    private final void fillLadder(MarketData data) {

        data.clear();
        final int rows = data.getRows();
        int row = 0;
        for (RbNode node = bidSide.getFirstLevel(); node != null
                && row < rows; node = node.rbNext()) {
            final Level level = (Level) node;
            data.setBidLevel(row++, level.getTicks(), level.getResd(), level.getQuot(),
                    level.getCount());
        }
        row = 0;
        for (RbNode node = offerSide.getFirstLevel(); node != null
                && row < rows; node = node.rbNext()) {
            final Level level = (Level) node;
            data.setOfferLevel(row++, level.getTicks(), level.getResd(), level.getQuot(),
                    level.getCount());
        }
    }

    MarketBook(String mnem, @Nullable String display, Memorable contr, int settlDay, int expiryDay,
            int state, long lastTicks, long lastLots, long lastTime, long maxOrderId,
            long maxExecId) {
        super(mnem, display, contr, settlDay, expiryDay, state);
        this.lastTicks = lastTicks;
        this.lastLots = lastLots;
        this.lastTime = lastTime;
        this.view = new MarketView(mnem, contr.getMnem(), settlDay, lastTicks, lastLots, lastTime);
        this.maxOrderId = maxOrderId;
        this.maxExecId = maxExecId;
    }

    public final void toJsonView(@Nullable Params params, Appendable out) throws IOException {
        int depth = 3; // Default depth.
        if (params != null) {
            final Integer val = params.getParam("depth", Integer.class);
            if (val != null) {
                depth = val.intValue();
            }
        }
        // Round-up to minimum.
        depth = Math.max(depth, 1);
        // Round-down to maximum.
        depth = Math.min(depth, DEPTH_MAX);

        out.append("{\"market\":\"").append(mnem);
        out.append("\",\"contr\":\"").append(contr.getMnem());
        out.append("\",\"settlDate\":");
        if (settlDay != 0) {
            out.append(String.valueOf(jdToIso(settlDay)));
        } else {
            out.append("null");
        }
        if (lastLots != 0) {
            out.append(",\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
            out.append(",\"lastTime\":").append(String.valueOf(lastTime));
        } else {
            out.append(",\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null");
        }

        out.append(",\"bidTicks\":[");
        final RbNode firstBid = bidSide.getFirstLevel();
        final RbNode firstOffer = offerSide.getFirstLevel();
        RbNode node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getTicks()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidResd\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getResd()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidQuot\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getQuot()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"bidCount\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getCount()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerTicks\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getTicks()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerResd\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getResd()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerQuot\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getQuot()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("],\"offerCount\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getCount()));
                node = node.rbNext();
            } else {
                out.append("null");
            }
        }
        out.append("]}");
    }

    public final void insertOrder(Order order) {
        getSide(order.getSide()).insertOrder(order);
    }

    public final void removeOrder(Order order) {
        getSide(order.getSide()).removeOrder(order);
    }

    public final void createOrder(Order order, long now) {
        getSide(order.getSide()).createOrder(order, now);
    }

    public final void reviseOrder(Order order, long lots, long now) {
        getSide(order.getSide()).reviseOrder(order, lots, now);
    }

    public final void cancelOrder(Order order, long now) {
        getSide(order.getSide()).cancelOrder(order, now);
    }

    public final void takeOrder(Order order, long lots, long now) {
        final BookSide side = getSide(order.getSide());
        side.takeOrder(order, lots, now);
        lastTicks = order.getTicks();
        lastLots = lots;
        lastTime = now;
    }

    public final long allocOrderId() {
        return ++maxOrderId;
    }

    public final long allocExecId() {
        return ++maxExecId;
    }

    public final long getLastTicks() {
        return lastTicks;
    }

    public final long getLastLots() {
        return lastLots;
    }

    public final long getLastTime() {
        return lastTime;
    }

    public final BookSide getBidSide() {
        return bidSide;
    }

    public final BookSide getOfferSide() {
        return offerSide;
    }

    public final MarketView getView() {
        return view;
    }

    public final long getMaxOrderId() {
        return maxOrderId;
    }

    public final long getMaxExecId() {
        return maxExecId;
    }

    public static MarketBook insertDirty(@Nullable final MarketBook first, MarketBook next) {
        if (first == null) {
            next.dirtyNext = null; // Defensive.
            return next;
        }

        MarketBook node = first;
        for (;;) {
            assert node != null;
            if (node == next) {
                // Entry already exists.
                break;
            } else if (node.dirtyNext == null) {
                next.dirtyNext = null; // Defensive.
                node.dirtyNext = next;
                break;
            }
            node = node.dirtyNext;
        }
        return first;
    }

    public final @Nullable MarketBook popDirty() {
        final MarketBook next = dirtyNext;
        dirtyNext = null;
        return next;
    }

    public final void updateView() {
        view.lastTicks = lastTicks;
        view.lastLots = lastLots;
        view.lastTime = lastTime;
        fillLadder(view.data);
    }
}
