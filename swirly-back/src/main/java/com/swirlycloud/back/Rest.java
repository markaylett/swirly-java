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

    private final boolean doGetRec(RecType recType, Appendable out) throws IOException {
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

    private final boolean doGetOrder(Accnt accnt, String email, Appendable out) throws IOException {
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

    private final boolean doGetTrade(Accnt accnt, String email, Appendable out) throws IOException {
        out.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    private final boolean doGetPosn(Accnt accnt, String email, Appendable out) throws IOException {
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

    public Rest(Model model) {
        serv = new Serv(model);
    }

    public final synchronized boolean getRec(Appendable out) throws IOException {
        out.append("{\"assets\":");
        if (!doGetRec(RecType.ASSET, out)) {
            return false;
        }
        out.append(",\"contrs\":");
        if (!doGetRec(RecType.CONTR, out)) {
            return false;
        }
        out.append("}");
        return true;
    }

    public final synchronized boolean getRec(RecType recType, Appendable out) throws IOException {
        return doGetRec(recType, out);
    }

    public final synchronized boolean getRec(RecType recType, String mnem, Appendable out)
            throws IOException {
        final Rec rec = serv.findRec(recType, mnem);
        if (rec == null) {
            return false;
        }
        rec.toJson(out, null);
        return true;
    }

    public final synchronized boolean postUser(String mnem, String display, String email,
            Appendable out) throws IOException {
        final User user = serv.createUser(mnem, display, email);
        user.toJson(out, null);
        return true;
    }

    public final synchronized boolean getMarket(Integer levels, Appendable out) throws IOException {
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

    public final synchronized boolean getMarket(String cmnem, Integer levels, Appendable out)
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

    public final synchronized boolean getMarket(String cmnem, int settlDate, Integer levels,
            Appendable out) throws IOException {
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

    public final synchronized boolean postMarket(String cmnem, int settlDate,
            int expiryDate, Appendable out) throws IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final int expiryDay = isoToJd(expiryDate);
        final Market market = serv.createMarket(contr, settlDay, expiryDay);
        market.toJson(out, null);
        return true;
    }

    public final synchronized boolean getAccnt(String email, Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("{\"orders\":[],\"trades\":[],\"posns\":[]}");
            return true;
        }
        out.append("{\"orders\":");
        if (!doGetOrder(accnt, email, out)) {
            return false;
        }
        out.append(",\"trades\":");
        if (!doGetTrade(accnt, email, out)) {
            return false;
        }
        out.append(",\"posns\":");
        if (!doGetPosn(accnt, email, out)) {
            return false;
        }
        out.append("}");
        return true;
    }

    public final synchronized boolean deleteOrder(String email, String cmnem, int settlDate, long id)
            throws IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        serv.archiveOrder(accnt, contr.getId(), settlDay, id);
        return true;
    }

    public final synchronized boolean getOrder(String email, Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        return doGetOrder(accnt, email, out);
    }

    public final synchronized boolean getOrder(String email, String cmnem, Appendable out)
            throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        out.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (order.getContrId() != contr.getId()) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            order.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getOrder(String email, String cmnem, int settlDate,
            Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        out.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (order.getContrId() != contr.getId() || order.getSettlDay() != settlDay) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            order.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getOrder(String email, String cmnem, int settlDate, long id,
            Appendable out) throws IOException {
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

    public final synchronized boolean postOrder(String email, String cmnem, int settlDate,
            String ref, Action action, long ticks, long lots, long minLots, Appendable out)
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

    public final synchronized boolean putOrder(String email, String cmnem, int settlDate, long id,
            long lots, Appendable out) throws IOException {
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

    public final synchronized boolean deleteTrade(String email, String cmnem, int settlDate, long id) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        serv.archiveTrade(accnt, contr.getId(), settlDay, id);
        return true;
    }

    public final synchronized boolean getTrade(String email, Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        return doGetTrade(accnt, email, out);
    }

    public final synchronized boolean getTrade(String email, String cmnem, Appendable out)
            throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        out.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (trade.getContrId() != contr.getId()) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getTrade(String email, String cmnem, int settlDate,
            Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
        final int settlDay = isoToJd(settlDate);
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        out.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (trade.getContrId() != contr.getId() || trade.getSettlDay() != settlDay) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(out, null);
            ++i;
        }
        out.append(']');
        return true;
    }

    public final synchronized boolean getTrade(String email, String cmnem, int settlDate, long id,
            Appendable out) throws IOException {
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

    public final synchronized boolean getPosn(String email, Appendable out) throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
        }
        return doGetPosn(accnt, email, out);
    }

    public final synchronized boolean getPosn(String email, String cmnem, Appendable out)
            throws IOException {
        final User user = serv.findUserByEmail(email);
        if (user == null) {
            return false;
        }
        if (serv.findRec(RecType.CONTR, cmnem) == null) {
            return false;
        }
        final Accnt accnt = serv.findAccnt(user);
        if (accnt == null) {
            out.append("[]");
            return true;
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

    public final synchronized boolean getPosn(String email, String cmnem, int settlDate,
            Appendable out) throws IOException {
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
