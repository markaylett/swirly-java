/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import static org.doobry.util.Date.isoToJd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.doobry.domain.Action;
import org.doobry.domain.Book;
import org.doobry.domain.Contr;
import org.doobry.domain.Exec;
import org.doobry.domain.Kind;
import org.doobry.domain.Order;
import org.doobry.domain.Posn;
import org.doobry.domain.Rec;
import org.doobry.domain.User;
import org.doobry.engine.Accnt;
import org.doobry.engine.Model;
import org.doobry.engine.Serv;
import org.doobry.engine.Trans;
import org.doobry.util.RbNode;
import org.doobry.util.SlNode;

public final class Rest {

    private final Serv serv;

    public Rest(Model model) {
        serv = new Serv(model);
    }

    public final synchronized Map<String, String> newUserMap() {
        final Map<String, String> m = new ConcurrentHashMap<>();
        for (SlNode node = serv.getFirstRec(Kind.USER); node != null; node = node.slNext()) {
            final User user = (User) node;
            m.put(user.getEmail(), user.getMnem());
        }
        return m;
    }

    public final synchronized void getRec(StringBuilder sb) {
        sb.append("{\"asset\":");
        getRec(sb, Kind.ASSET);
        sb.append(",\"contr\":");
        getRec(sb, Kind.CONTR);
        sb.append("}");
    }

    public final synchronized void getRec(StringBuilder sb, Kind kind) {
        sb.append('[');
        SlNode node = serv.getFirstRec(kind);
        for (int i = 0; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (i > 0) {
                sb.append(',');
            }
            rec.print(sb, null);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized boolean getRec(StringBuilder sb, Kind kind, String mnem) {
        final Rec rec = serv.findRec(kind, mnem);
        if (rec == null) {
            return false;
        }
        rec.print(sb, null);
        return true;
    }

    public final synchronized void getAccnt(StringBuilder sb, String umnem) {
        sb.append("{\"order\":");
        getOrder(sb, umnem);
        sb.append(",\"trade\":");
        getTrade(sb, umnem);
        sb.append(",\"posn\":");
        getPosn(sb, umnem);
        sb.append("}");
    }

    public final synchronized void getOrder(StringBuilder sb, String umnem) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        sb.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (i > 0) {
                sb.append(',');
            }
            order.print(sb, null);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized boolean getOrder(StringBuilder sb, String umnem, long id) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        final Order order = accnt.findOrder(id);
        if (order == null) {
            return false;
        }
        order.print(sb, null);
        return true;
    }

    public final synchronized void postOrder(StringBuilder sb, String umnem, String cmnem,
            int settlDate, String ref, Action action, long ticks, long lots, long minLots) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        final Book book = serv.getLazyBook(cmnem, isoToJd(settlDate));
        final Order order = serv.placeOrder(accnt, book, ref, action, ticks, lots, minLots,
                new Trans()).getOrder();
        order.print(sb, null);
    }

    public final synchronized void putOrder(StringBuilder sb, String umnem, long id, long lots) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        final Order order = lots > 0 ? serv.reviseOrder(accnt, id, lots) //
                : serv.cancelOrder(accnt, id);
        order.print(sb, null);
    }

    public final synchronized void getTrade(StringBuilder sb, String umnem) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        sb.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (i++ > 0) {
                sb.append(',');
            }
            trade.print(sb, null);
        }
        sb.append(']');
    }

    public final synchronized boolean getTrade(StringBuilder sb, String umnem, long id) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        final Exec trade = accnt.findTrade(id);
        if (trade == null) {
            return false;
        }
        trade.print(sb, null);
        return true;
    }

    public final synchronized void deleteTrade(StringBuilder sb, String umnem, long id) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        serv.ackTrade(accnt, id);
    }

    public final synchronized void getPosn(StringBuilder sb, String umnem) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        sb.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (i > 0) {
                sb.append(',');
            }
            posn.print(sb, null);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized void getPosn(StringBuilder sb, String umnem, String cmnem) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        sb.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (!posn.getContr().getMnem().equals(cmnem)) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            posn.print(sb, null);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized boolean getPosn(StringBuilder sb, String umnem, String cmnem,
            int settlDate) {
        final Accnt accnt = serv.getLazyAccnt(umnem);
        final Contr contr = (Contr) serv.findRec(Kind.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Posn posn = accnt.findPosn(contr, settlDay);
        if (posn == null) {
            return false;
        }
        posn.print(sb, null);
        return true;
    }

    public final synchronized void getBook(StringBuilder sb, Integer levels) {
        sb.append('[');
        RbNode node = serv.getFirstBook();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Book book = (Book) node;
            if (i > 0) {
                sb.append(',');
            }
            book.print(sb, levels);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized void getBook(StringBuilder sb, String cmnem, Integer levels) {
        sb.append('[');
        RbNode node = serv.getFirstBook();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Book book = (Book) node;
            if (!book.getContr().getMnem().equals(cmnem)) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            book.print(sb, levels);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized boolean getBook(StringBuilder sb, String cmnem, int settlDate,
            Integer levels) {
        final Contr contr = (Contr) serv.findRec(Kind.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Book book = serv.findBook(contr, settlDay);
        if (book == null) {
            return false;
        }
        book.print(sb, levels);
        return true;
    }
}
