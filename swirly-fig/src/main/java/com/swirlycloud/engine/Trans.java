/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import java.io.IOException;

import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.util.Jsonifiable;
import com.swirlycloud.util.Queue;
import com.swirlycloud.util.SlNode;
import com.swirlycloud.util.StringUtil;

public final class Trans implements Jsonifiable {
    private Market market;
    private Order order;
    final Queue matches = new Queue();
    /**
     * All executions referenced in matches.
     */
    final Queue execs = new Queue();
    /**
     * Optional taker position.
     */
    Posn posn;

    final void init(Market market, Order order, Exec exec) {
        assert market != null;
        assert order != null;
        assert exec != null;
        this.market = market;
        this.order = order;
        matches.clear();
        execs.clear();
        execs.insertBack(exec);
        posn = null;
    }

    @Override
    public final String toString() {
        return StringUtil.toJson(this);
    }

    @Override
    public final void toJson(Appendable out) throws IOException {
        final long traderId = order.getTraderId();
        out.append("{\"market\":");
        market.toJson(out);
        // Multiple orders may be updated if one trades with one's self.
        out.append(",\"orders\":[");
        order.toJson(out);
        for (SlNode node = matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            if (match.makerOrder.getTraderId() != traderId) {
                continue;
            }
            out.append(',');
            match.makerOrder.toJson(out);
        }
        out.append("],\"execs\":[");
        int i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (exec.getTraderId() != traderId) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            exec.toJson(out);
            ++i;
        }
        out.append("],\"posn\":");
        if (posn != null) {
            posn.toJson(out);
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
