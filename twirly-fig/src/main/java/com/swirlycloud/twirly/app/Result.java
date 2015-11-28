/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.book.MarketBook;
import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.entity.Posn;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonable;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class Result implements AutoCloseable, Jsonable {
    private @Nullable String trader;
    private @Nullable MarketBook book;
    final SlQueue orders = new SlQueue();
    final SlQueue matches = new SlQueue();
    /**
     * All executions referenced in matches.
     */
    final SlQueue execs = new SlQueue();
    /**
     * Optional taker position.
     */
    @Nullable
    Posn posn;

    final void reset(String trader, MarketBook book) {
        this.trader = trader;
        this.book = book;
        clear();
    }

    final void reset(String trader, MarketBook book, Order order, Exec exec) {
        reset(trader, book);
        orders.insertBack(order);
        execs.insertBack(exec);
    }

    /**
     * Prepare execs by cloning the jslNode list from the slNode list.
     * 
     * @return the cloned slNode list.
     */
    final @Nullable JslNode prepareExecList() {
        final Exec first = (Exec) execs.getFirst();
        Exec node = first;
        while (node != null) {
            final Exec next = (Exec) node.slNext();
            node.setJslNext(next);
            node = next;
        }
        return first;
    }

    @Override
    public final String toString() {
        return JsonUtil.toJson(this);
    }

    @Override
    public final void close() {
        clear();
    }

    @Override
    public final void toJson(@Nullable Params params, Appendable out) throws IOException {
        // Multiple orders may be updated if one trades with one's self.
        out.append("{\"orders\":[");
        int i = 0;
        for (SlNode node = orders.getFirst(); node != null; node = node.slNext()) {
            final Order order = (Order) node;
            // Filter-out previously quoted orders.
            if (order.getQuoteId() != 0) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            order.toJson(params, out);
            ++i;
        }
        for (SlNode node = matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            // Filter-out counter-party trades.
            if (!match.makerOrder.getTrader().equals(trader)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            match.makerOrder.toJson(params, out);
            ++i;
        }
        out.append("],\"execs\":[");
        i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (!exec.getTrader().equals(trader)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            exec.toJson(params, out);
            ++i;
        }
        out.append("],\"posn\":");
        final Posn posn = this.posn;
        if (posn != null) {
            posn.toJson(params, out);
        } else {
            out.append("null");
        }
        out.append(",\"view\":");
        final MarketBook book = this.book;
        assert book != null;
        book.toJsonView(params, out);
        out.append('}');
    }

    public final void clear() {
        orders.clearAll();
        matches.clear();
        execs.clearAll();
        posn = null;
    }

    public final @Nullable SlNode getFirstOrder() {
        return orders.getFirst();
    }

    public final @Nullable SlNode getFirstExec() {
        return execs.getFirst();
    }

    public final boolean isEmptyOrder() {
        return orders.isEmpty();
    }

    public final boolean isEmptyExec() {
        return execs.isEmpty();
    }

    public final @Nullable Posn getPosn() {
        return posn;
    }
}
