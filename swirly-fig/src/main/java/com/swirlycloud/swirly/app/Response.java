/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.app;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.book.MarketBook;
import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.entity.Posn;
import com.swirlycloud.swirly.intrusive.SlQueue;
import com.swirlycloud.swirly.node.JslNode;
import com.swirlycloud.swirly.node.SlNode;
import com.swirlycloud.swirly.node.SlUtil;
import com.swirlycloud.swirly.util.JsonUtil;
import com.swirlycloud.swirly.util.Jsonable;
import com.swirlycloud.swirly.util.Params;

public final @NonNullByDefault class Response implements AutoCloseable, Jsonable {
    @Nullable MarketBook book;
    final SlQueue orders = new SlQueue();
    /**
     * All executions referenced in matches.
     */
    final SlQueue execs = new SlQueue();
    /**
     * Optional taker position.
     */
    @Nullable
    Posn posn;

    private static void clearAll(SlQueue slq) {
        SlUtil.nullify(slq.getFirst());
        slq.clear();
    }

    private static void clearTail(SlQueue slq) {
        final SlNode first = slq.getFirst();
        if (first != null) {
            SlUtil.nullify(first.slNext());
        }
        slq.clearTail();
    }

    final void reset(MarketBook book) {
        this.book = book;
        clearAll();
    }

    final void reset(MarketBook book, Order order, Exec exec) {
        reset(book);
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
        clearAll();
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
        out.append("],\"execs\":[");
        i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
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

    public final void clearAll() {
        clearAll(orders);
        clearAll(execs);
        posn = null;
    }

    public final void clearMatches() {
        clearTail(orders);
        clearTail(execs);
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
