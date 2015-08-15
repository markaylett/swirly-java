/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.jdToIso;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Memorable;
import com.swirlycloud.twirly.util.Params;

/**
 * A place where buyers and sellers come together to exchange goods or services.
 * 
 * @author Mark Aylett
 */
public final @NonNullByDefault class MarketBook extends Market {

    private static final long serialVersionUID = 1L;
    /**
     * Maximum price levels in view.
     */
    private static final int DEPTH_MAX = 5;

    // Two sides constitute the book.
    private final transient BookSide bidSide = new BookSide();
    private final transient BookSide offerSide = new BookSide();

    private final BookSide getSide(Action action) {
        return action == Action.BUY ? bidSide : offerSide;
    }

    MarketBook(String mnem, @Nullable String display, Memorable contr, int settlDay, int expiryDay,
            int state, long lastTicks, long lastLots, long lastTime, long maxOrderId, long maxExecId) {
        super(mnem, display, contr, settlDay, expiryDay, state, lastTicks, lastLots, lastTime,
                maxOrderId, maxExecId);
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
        out.append("],\"bidLots\":[");
        node = firstBid;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getLots()));
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
        out.append("],\"offerLots\":[");
        node = firstOffer;
        for (int i = 0; i < depth; ++i) {
            if (i > 0) {
                out.append(',');
            }
            if (node != null) {
                final Level level = (Level) node;
                out.append(String.valueOf(level.getLots()));
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
        if (lastLots != 0) {
            out.append("],\"lastTicks\":").append(String.valueOf(lastTicks));
            out.append(",\"lastLots\":").append(String.valueOf(lastLots));
            out.append(",\"lastTime\":").append(String.valueOf(lastTime));
        } else {
            out.append("],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null");
        }
        out.append('}');
    }

    final void insertOrder(Order order) {
        getSide(order.getAction()).insertOrder(order);
    }

    final void removeOrder(Order order) {
        getSide(order.getAction()).removeOrder(order);
    }

    final void placeOrder(Order order, long now) {
        getSide(order.getAction()).placeOrder(order, now);
    }

    final void reviseOrder(Order order, long lots, long now) {
        getSide(order.getAction()).reviseOrder(order, lots, now);
    }

    final void cancelOrder(Order order, long now) {
        getSide(order.getAction()).cancelOrder(order, now);
    }

    final void takeOrder(Order order, long lots, long now) {
        final BookSide side = getSide(order.getAction());
        side.takeOrder(order, lots, now);
        lastTicks = order.getTicks();
        lastLots = lots;
        lastTime = now;
    }

    final BookSide getBidSide() {
        return bidSide;
    }

    final BookSide getOfferSide() {
        return offerSide;
    }
}
