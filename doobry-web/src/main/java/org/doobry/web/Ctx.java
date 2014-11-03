/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.doobry.util.Date.isoToJd;

import org.doobry.domain.Action;
import org.doobry.domain.Exec;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.RecType;
import org.doobry.domain.Reg;
import org.doobry.engine.Accnt;
import org.doobry.engine.Book;
import org.doobry.engine.Serv;
import org.doobry.mock.MockBank;
import org.doobry.mock.MockJourn;
import org.doobry.mock.MockModel;
import org.doobry.util.RbNode;
import org.doobry.util.SlNode;

public final class Ctx {
    private static class CtxHolder {
        private static final Ctx INSTANCE = new Ctx();
    }

    private final Serv serv;

    private Ctx() {
        serv = new Serv(new MockBank(Reg.values().length), new MockJourn());
        serv.load(new MockModel());
    }

    public static Ctx getInstance() {
        return CtxHolder.INSTANCE;
    }

    public final synchronized CharSequence getRec(final RecType type) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        SlNode node = serv.getFirstRec(type);
        for (int i = 0; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (i++ > 0) {
                sb.append(',');
            }
            rec.print(sb);
        }
        sb.append(']');
        return sb;
    }

    public final synchronized CharSequence getRec(final RecType type, final String mnem) {
        final StringBuilder sb = new StringBuilder();
        final Rec rec = serv.findRecMnem(type, mnem);
        if (rec != null) {
            rec.print(sb);
        }
        return sb;
    }

    public final synchronized CharSequence getOrder(final String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (i++ > 0) {
                sb.append(',');
            }
            order.print(sb);
        }
        sb.append(']');
        return sb;
    }

    public final synchronized CharSequence getOrder(final String user, final long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final StringBuilder sb = new StringBuilder();
        final Order order = accnt.findOrderId(id);
        if (order != null) {
            order.print(sb);
        }
        return sb;
    }

    public final synchronized CharSequence postOrder(final String user, final String contr,
            final int settlDate, final String ref, final Action action, final long ticks,
            final long lots, final long minLots) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Book book = serv.getLazyBook(contr, isoToJd(settlDate));
        final Order order = serv.placeOrder(accnt, book, ref, action, ticks, lots, minLots);
        final StringBuilder sb = new StringBuilder();
        order.print(sb);
        return sb;
    }

    public final synchronized CharSequence putOrder(final String user, final long id,
            final long lots) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Order order = lots > 0 ? serv.reviseOrderId(accnt, id, lots) : serv.cancelOrderId(
                accnt, id);
        final StringBuilder sb = new StringBuilder();
        order.print(sb);
        return sb;
    }

    public final synchronized CharSequence getTrade(final String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (i++ > 0) {
                sb.append(',');
            }
            trade.print(sb);
        }
        sb.append(']');
        return sb;
    }

    public final synchronized CharSequence getTrade(final String user, final long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final StringBuilder sb = new StringBuilder();
        final Exec trade = accnt.findTradeId(id);
        if (trade != null) {
            trade.print(sb);
        }
        return sb;
    }

    public final synchronized CharSequence deleteTrade(final String user, final long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        serv.ackTrade(accnt, id);
        return "";
    }

    public final synchronized CharSequence getPosn(final String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (i++ > 0) {
                sb.append(',');
            }
            posn.print(sb);
        }
        sb.append(']');
        return sb;
    }
}
