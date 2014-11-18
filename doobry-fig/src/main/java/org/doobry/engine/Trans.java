/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.engine;

import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
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
    Posn posn;

    final void clear() {
        order = null;
        matches.clear();
        execs.clear();
        posn = null;
    }

    @Override
    public void print(StringBuilder sb, Object arg) {
        sb.append("{\"order\":").append(order);
        sb.append(",\"exec\":[");
        SlNode node = execs.getFirst();
        for (int i = 0; node != null; node = node.slNext()) {
            final Exec exec = (Exec) node;
            if (i > 0) {
                sb.append(',');
            }
            exec.print(sb, null);
            ++i;
        }
        // Position is optional.
        if (posn != null) {
            sb.append("],\"posn\":").append(posn);
            sb.append("}");
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
