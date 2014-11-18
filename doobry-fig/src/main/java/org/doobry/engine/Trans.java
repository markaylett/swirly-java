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
    Order newOrder;
    final Queue matches = new Queue();
    /**
     * All executions referenced in matches.
     */
    final Queue execs = new Queue();
    Posn takerPosn;

    final void clear() {
        newOrder = null;
        matches.clear();
        execs.clear();
        takerPosn = null;
    }

    @Override
    public void print(StringBuilder sb, Object arg) {

        if (newOrder != null) {
            sb.append("{\"newOrder\":").append(newOrder);
            sb.append(',');
        } else {
            sb.append('{');
        }
        sb.append("\"exec\":[");
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
        if (takerPosn != null) {
            sb.append("],\"takerPosn\":").append(takerPosn);
            sb.append('}');
        } else {
            sb.append("]}");
        }
    }

    public final Order getNewOrder() {
        return newOrder;
    }

    public final SlNode getFirstExec() {
        return execs.getFirst();
    }

    public final boolean isEmptyExec() {
        return execs.isEmpty();
    }

    public final Posn getTakerPosn() {
        return takerPosn;
    }
}
