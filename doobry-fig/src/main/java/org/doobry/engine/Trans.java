/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.User;
import org.doobry.util.Printable;
import org.doobry.util.Queue;
import org.doobry.util.SlNode;

public final class Trans implements Printable {
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
        order = null;
        matches.clear();
        execs.clear();
        posn = null;
    }

    @Override
    public final void print(StringBuilder sb, Object arg) {
        final User user = (User) arg;
        sb.append("{\"order\":[");
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
        sb.append("],\"exec\":[");
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
            sb.append("],\"posn\":").append(posn);
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
