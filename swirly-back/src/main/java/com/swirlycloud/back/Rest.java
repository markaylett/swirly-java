/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import static com.swirlycloud.util.Date.isoToJd;

import java.io.IOException;

import com.swirlycloud.domain.Action;
import com.swirlycloud.domain.Contr;
import com.swirlycloud.domain.Exec;
import com.swirlycloud.domain.Market;
import com.swirlycloud.domain.Order;
import com.swirlycloud.domain.Posn;
import com.swirlycloud.domain.Rec;
import com.swirlycloud.domain.RecType;
import com.swirlycloud.domain.User;
import com.swirlycloud.engine.Accnt;
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

    public final synchronized boolean getRec(Appendable out) throws IOException {
        out.append("{\"assets\":");
        getRec(out, RecType.ASSET);
        out.append(",\"contrs\":");
        getRec(out, RecType.CONTR);
        out.append("}");
        return true;
    }

    public final synchronized boolean getRec(Appendable out, RecType recType) throws IOException {
        out.append('[');
        SlNode node = serv.getFirstRec(recType);
        for (int i = 0; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (i > 0) {
                out.append(',');
            }
            rec.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getRec(Appendable out, RecType recType, String mnem)
            throws IOException {
        final Rec rec = serv.findRec(recType, mnem);
        if (rec == null) {
            return false;
        }
        rec.toJson(out, null);
        return true;
    }

    public final synchronized void registerUser(Appendable out, String mnem, String display,
            String email) throws IOException {
        final User user = serv.registerUser(mnem, display, email);
        user.toJson(out, null);
    }

    public final synchronized boolean getMarket(Appendable out, Integer levels)
            throws IOException {
        out.append('[');
        RbNode node = serv.getFirstMarket();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (i > 0) {
                out.append(',');
            }
            market.toJson(out, levels);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getMarket(Appendable out, String cmnem, Integer levels)
            throws IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        out.append('[');
        RbNode node = serv.getFirstMarket();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (!market.getContr().getMnem().equals(cmnem)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            market.toJson(out, levels);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getMarket(Appendable out, String cmnem, int settlDate,
            Integer levels) throws IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Market market = serv.findMarket(contr, settlDay);
        if (market == null) {
            return false;
        }
        market.toJson(out, levels);
        return true;
    }

    public final synchronized boolean getAccnt(Appendable out, String email) throws IOException {
        out.append("{\"orders\":");
        getOrder(out, email);
        out.append(",\"trades\":");
        getTrade(out, email);
        out.append(",\"posns\":");
        getPosn(out, email);
        out.append("}");
        return true;
    }

    public final synchronized boolean deleteOrder(Appendable out, String email, String cmnem,
            int settlDate, long id) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Market market = serv.findMarket(cmnem, settlDay);
        if (market == null) {
            return false;
        }
        final Trans trans = serv.cancelOrder(accnt, market, id, new Trans());
        trans.toJson(out, null);
        return true;
    }

    public final synchronized boolean getOrder(Appendable out, String email) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        out.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (i > 0) {
                out.append(',');
            }
            order.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getOrder(Appendable out, String email, String cmnem,
            int settlDate, long id) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Order order = accnt.findOrder(contr.getId(), settlDay, id);
        if (order == null) {
            return false;
        }
        order.toJson(out, null);
        return true;
    }

    public final synchronized boolean postOrder(Appendable out, String email, String cmnem,
            int settlDate, String ref, Action action, long ticks, long lots, long minLots)
            throws IOException {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Market market = serv.getLazyMarket(cmnem, isoToJd(settlDate));
        if (market == null) {
            return false;
        }
        final Trans trans = serv.placeOrder(accnt, market, ref, action, ticks, lots, minLots,
                new Trans());
        trans.toJson(out, accnt.getUser());
        return true;
    }

    public final synchronized boolean putOrder(Appendable out, String email, String cmnem,
            int settlDate, long id, long lots) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Market market = serv.findMarket(cmnem, isoToJd(settlDate));
        if (market == null) {
            return false;
        }
        final Trans trans = new Trans();
        if (lots > 0) {
            serv.reviseOrder(accnt, market, id, lots, trans);
        } else {
            serv.cancelOrder(accnt, market, id, trans);
        }
        trans.toJson(out, null);
        return true;
    }

    public final synchronized boolean getTrade(Appendable out, String email) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        out.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (i++ > 0) {
                out.append(',');
            }
            trade.toJson(out, null);
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getTrade(Appendable out, String email, String cmnem,
            int settlDate, long id) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Exec trade = accnt.findTrade(contr.getId(), settlDay, id);
        if (trade == null) {
            return false;
        }
        trade.toJson(out, null);
        return true;
    }

    public final synchronized boolean deleteTrade(String email, String cmnem,
            int settlDate, long id) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        serv.confirmTrade(accnt, contr.getId(), settlDay, id);
        return true;
    }

    public final synchronized boolean getPosn(Appendable out, String email) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        out.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (i > 0) {
                out.append(',');
            }
            posn.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getPosn(Appendable out, String email, String cmnem)
            throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        out.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (!posn.getContr().getMnem().equals(cmnem)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            posn.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getPosn(Appendable out, String email, String cmnem,
            int settlDate) throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Posn posn = accnt.findPosn(contr, settlDay);
        if (posn == null) {
            return false;
        }
        posn.toJson(out, null);
        return true;
    }
}
