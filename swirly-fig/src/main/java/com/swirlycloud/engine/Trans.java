/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import static com.swirlycloud.engine.Constants.DEPTH;

import java.io.IOException;

import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Trader;
import com.swirlycloud.util.AshUtil;
import com.swirlycloud.util.Jsonifiable;
import com.swirlycloud.util.Queue;
import com.swirlycloud.util.SlNode;

public final class Trans implements Jsonifiable {
    Market market;
    Order order;
    final Queue matches = new Queue();
    /**
     * All executions referenced in matches.
     */
    final Queue execs = new Queue();
    /**
     * Optional taker position.
     */
    Posn posn;

    final void clear() {
        market = null;
        order = null;
        matches.clear();
        execs.clear();
        posn = null;
    }

    @Override
    public final String toString() {
        return AshUtil.toJson(this, null);
    }

    @Override
    public final void toJson(Appendable out, Object arg) throws IOException {
        final Trader trader = (Trader) arg;
        out.append("{\"market\":");
        if (market != null) {
            market.toJson(out, DEPTH);
        } else {
            out.append("null");
        }
        out.append(",\"orders\":[");
        int i = 0;
        if (order != null) {
            assert trader != null && order.getTraderId() == trader.getId();
            order.toJson(out, null);
            ++i;
        }
        for (SlNode node = matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            if (trader != null && match.makerOrder.getTraderId() != trader.getId()) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            match.makerOrder.toJson(out, null);
            ++i;
        }
        out.append("],\"execs\":[");
        i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (trader != null && exec.getTraderId() != trader.getId()) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            exec.toJson(out, null);
            ++i;
        }
        out.append("],\"posn\":");
        if (posn != null) {
            posn.toJson(out, null);
        } else {
            out.append("null");
        }
        out.append('}');
    }

    public final Order getOrder() {
        return order;
    }

    public final SlNode getFirstExec() {
        return execs.getFirst();
    }

    public final boolean isEmptyExec() {
        return execs.isEmpty();
    }

    public final Posn getPosn() {
        return posn;
    }
}
