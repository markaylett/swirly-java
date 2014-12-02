/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.engine;

import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.User;
import com.swirlycloud.util.Printable;
import com.swirlycloud.util.Queue;
import com.swirlycloud.util.SlNode;

public final class Trans implements Printable {
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
    public final void print(StringBuilder sb, Object arg) {
        final User user = (User) arg;
        if (market != null) {
            sb.append("{\"market\":");
            // FIXME: number of levels.
            market.print(sb, Integer.valueOf(5));
            sb.append(',');
        } else {
            sb.append('{');
        }
        sb.append("\"orders\":[");
        int i = 0;
        if (order != null) {
            assert user != null && order.getUserId() == user.getId();
            order.print(sb, null);
            ++i;
        }
        for (SlNode node = matches.getFirst(); node != null; node = node.slNext()) {
            final Match match = (Match) node;
            if (user != null && match.makerOrder.getUserId() != user.getId()) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            match.makerOrder.print(sb, null);
            ++i;
        }
        sb.append("],\"execs\":[");
        i = 0;
        for (SlNode node = execs.getFirst(); node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (user != null && exec.getUserId() != user.getId()) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            exec.print(sb, null);
            ++i;
        }
        // Position is optional.
        if (posn != null) {
            sb.append("],\"posn\":");
            posn.print(sb, null);
            sb.append('}');
        } else {
            sb.append("]}");
        }
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
