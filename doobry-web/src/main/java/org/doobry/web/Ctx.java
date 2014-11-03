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

    public final synchronized void getRec(StringBuilder sb) {
        sb.append("{\"asset\":");
        getRec(sb, RecType.ASSET);
        sb.append(",\"contr\":");
        getRec(sb, RecType.CONTR);
        sb.append("}");
    }

    public final synchronized void getRec(StringBuilder sb, RecType type) {
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
    }

    public final synchronized void getRec(StringBuilder sb, RecType type, String mnem) {
        final Rec rec = serv.findRecMnem(type, mnem);
        if (rec != null) {
            rec.print(sb);
        }
    }

    public final synchronized void getOrder(StringBuilder sb, String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
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
    }

    public final synchronized void getOrder(StringBuilder sb, String user, long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Order order = accnt.findOrderId(id);
        if (order != null) {
            order.print(sb);
        }
    }

    public final synchronized void postOrder(StringBuilder sb, String user, String contr,
            int settlDate, String ref, Action action, long ticks, long lots, long minLots) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Book book = serv.getLazyBook(contr, isoToJd(settlDate));
        final Order order = serv.placeOrder(accnt, book, ref, action, ticks, lots, minLots);
        order.print(sb);
    }

    public final synchronized void putOrder(StringBuilder sb, String user, long id, long lots) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Order order = lots > 0 ? serv.reviseOrderId(accnt, id, lots) : serv.cancelOrderId(
                accnt, id);
        order.print(sb);
    }

    public final synchronized void getTrade(StringBuilder sb, String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
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
    }

    public final synchronized void getTrade(StringBuilder sb, String user, long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        final Exec trade = accnt.findTradeId(id);
        if (trade != null) {
            trade.print(sb);
        }
    }

    public final synchronized void deleteTrade(StringBuilder sb, String user, long id) {
        final Accnt accnt = serv.getLazyAccnt(user);
        serv.ackTrade(accnt, id);
    }

    public final synchronized void getPosn(StringBuilder sb, String user) {
        final Accnt accnt = serv.getLazyAccnt(user);
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
    }

    public final synchronized void getPosn(StringBuilder sb, String user, String contr) {
    }

    public final synchronized void getPosn(StringBuilder sb, String user, String contr,
            int settlDate) {
    }

    public final synchronized void getBook(StringBuilder sb) {
    }

    public final synchronized void getBook(StringBuilder sb, String contr) {
    }

    public final synchronized void getBook(StringBuilder sb, String contr, int settlDate) {
    }
}
