/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.Date.isoToJd;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Kind;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Accnt;
import com.swirlycloud.engine.Market;
import com.swirlycloud.engine.Model;
import com.swirlycloud.engine.Serv;
import com.swirlycloud.engine.Trans;
import com.swirlycloud.util.RbNode;
import com.swirlycloud.util.SlNode;

public final class Rest {

    private final Serv serv;

    public Rest(Model model) {
        serv = new Serv(model);
    }

    public final synchronized void getRec(StringBuilder sb) {
        sb.append("{\"assets\":");
        getRec(sb, Kind.ASSET);
        sb.append(",\"contrs\":");
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

    public final synchronized void registerUser(StringBuilder sb, String mnem, String display,
            String email) {
        final User user = serv.registerUser(mnem, display, email);
        user.print(sb, null);
    }

    public final synchronized void getMarket(StringBuilder sb, Integer levels) {
        sb.append('[');
        RbNode node = serv.getFirstMarket();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (i > 0) {
                sb.append(',');
            }
            market.print(sb, levels);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized void getMarket(StringBuilder sb, String cmnem, Integer levels) {
        sb.append('[');
        RbNode node = serv.getFirstMarket();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (!market.getContr().getMnem().equals(cmnem)) {
                continue;
            }
            if (i > 0) {
                sb.append(',');
            }
            market.print(sb, levels);
            ++i;
        }
        sb.append(']');
    }

    public final synchronized boolean getMarket(StringBuilder sb, String cmnem, int settlDate,
            Integer levels) {
        final Contr contr = (Contr) serv.findRec(Kind.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Market market = serv.findMarket(contr, settlDay);
        if (market == null) {
            return false;
        }
        market.print(sb, levels);
        return true;
    }

    public final synchronized void getAccnt(StringBuilder sb, String email) {
        sb.append("{\"orders\":");
        getOrder(sb, email);
        sb.append(",\"trades\":");
        getTrade(sb, email);
        sb.append(",\"posns\":");
        getPosn(sb, email);
        sb.append("}");
    }

    public final synchronized void deleteOrder(StringBuilder sb, String email, long id) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Trans trans = serv.cancelOrder(accnt, id, new Trans());
        trans.print(sb, null);
    }

    public final synchronized void getOrder(StringBuilder sb, String email) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
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

    public final synchronized boolean getOrder(StringBuilder sb, String email, long id) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Order order = accnt.findOrder(id);
        if (order == null) {
            return false;
        }
        order.print(sb, null);
        return true;
    }

    public final synchronized void postOrder(StringBuilder sb, String email, String cmnem,
            int settlDate, String ref, Action action, long ticks, long lots, long minLots) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Market market = serv.getLazyMarket(cmnem, isoToJd(settlDate));
        final Trans trans = serv.placeOrder(accnt, market, ref, action, ticks, lots, minLots,
                new Trans());
        trans.print(sb, accnt.getUser());
    }

    public final synchronized void putOrder(StringBuilder sb, String email, long id, long lots) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Trans trans = new Trans();
        if (lots > 0) {
            serv.reviseOrder(accnt, id, lots, trans);
        } else {
            serv.cancelOrder(accnt, id, trans);
        }
        trans.print(sb, null);
    }

    public final synchronized void getTrade(StringBuilder sb, String email) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
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

    public final synchronized boolean getTrade(StringBuilder sb, String email, long id) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Exec trade = accnt.findTrade(id);
        if (trade == null) {
            return false;
        }
        trade.print(sb, null);
        return true;
    }

    public final synchronized void deleteTrade(StringBuilder sb, String email, long id) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        serv.confirmTrade(accnt, id);
    }

    public final synchronized void getPosn(StringBuilder sb, String email) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
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

    public final synchronized void getPosn(StringBuilder sb, String email, String cmnem) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
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

    public final synchronized boolean getPosn(StringBuilder sb, String email, String cmnem,
            int settlDate) {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
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
}
