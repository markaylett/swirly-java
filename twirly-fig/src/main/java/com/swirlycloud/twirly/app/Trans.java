/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import java.io.IOException;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.intrusive.SlQueue;
import com.swirlycloud.twirly.node.SlNode;
import com.swirlycloud.twirly.util.JsonUtil;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Params;

public final class Trans implements Jsonifiable {
    private Market market;
    private Order order;
    final SlQueue matches = new SlQueue();
    /**
     * All executions referenced in matches.
     */
    final SlQueue execs = new SlQueue();
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
        return JsonUtil.toJson(this);
    }

    @Override
    public final void toJson(Params params, Appendable out) throws IOException {
        final String trader = order.getTrader();
        out.append("{\"view\":");
        market.toJsonView(params, out);
        // Multiple orders may be updated if one trades with one's self.
        out.append(",\"orders\":[");
        order.toJson(params, out);
        for (SlNode node = matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            if (match.makerOrder.getTrader().equals(trader)) {
                continue;
            }
            out.append(',');
            match.makerOrder.toJson(params, out);
        }
        out.append("],\"execs\":[");
        int i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (exec.getTrader().equals(trader)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            exec.toJson(params, out);
            ++i;
        }
        out.append("],\"posn\":");
        if (posn != null) {
            posn.toJson(params, out);
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
