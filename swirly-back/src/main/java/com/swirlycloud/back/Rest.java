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

    public final synchronized boolean getRec(StringBuilder sb) {
        sb.append("{\"assets\":");
        getRec(sb, RecType.ASSET);
        sb.append(",\"contrs\":");
        getRec(sb, RecType.CONTR);
        sb.append("}");
        return true;
    }

    public final synchronized boolean getRec(StringBuilder sb, RecType recType) {
        sb.append('[');
        SlNode node = serv.getFirstRec(recType);
        for (int i = 0; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (i > 0) {
                sb.append(',');
            }
            rec.print(sb, null);
            ++i;
        }
        sb.append(']');
        return true;
    }

    public final synchronized boolean getRec(StringBuilder sb, RecType recType, String mnem) {
        final Rec rec = serv.findRec(recType, mnem);
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

    public final synchronized boolean getMarket(StringBuilder sb, Integer levels) {
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
        return true;
    }

    public final synchronized boolean getMarket(StringBuilder sb, String cmnem, Integer levels) {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            return false;
        }
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
        return true;
    }

    public final synchronized boolean getMarket(StringBuilder sb, String cmnem, int settlDate,
            Integer levels) {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
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

    public final synchronized boolean getAccnt(StringBuilder sb, String email) {
        sb.append("{\"orders\":");
        getOrder(sb, email);
        sb.append(",\"trades\":");
        getTrade(sb, email);
        sb.append(",\"posns\":");
        getPosn(sb, email);
        sb.append("}");
        return true;
    }

    public final synchronized boolean deleteOrder(StringBuilder sb, String email, String cmnem,
            int settlDate, long id) {
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
        trans.print(sb, null);
        return true;
    }

    public final synchronized boolean getOrder(StringBuilder sb, String email) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
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
        return true;
    }

    public final synchronized boolean getOrder(StringBuilder sb, String email, String cmnem,
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
        final Order order = accnt.findOrder(contr.getId(), settlDay, id);
        if (order == null) {
            return false;
        }
        order.print(sb, null);
        return true;
    }

    public final synchronized boolean postOrder(StringBuilder sb, String email, String cmnem,
            int settlDate, String ref, Action action, long ticks, long lots, long minLots) {
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
        trans.print(sb, accnt.getUser());
        return true;
    }

    public final synchronized boolean putOrder(StringBuilder sb, String email, String cmnem,
            int settlDate, long id, long lots) {
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
        trans.print(sb, null);
        return true;
    }

    public final synchronized boolean getTrade(StringBuilder sb, String email) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
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
        return true;
    }

    public final synchronized boolean getTrade(StringBuilder sb, String email, String cmnem,
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
        final Exec trade = accnt.findTrade(contr.getId(), settlDay, id);
        if (trade == null) {
            return false;
        }
        trade.print(sb, null);
        return true;
    }

    public final synchronized boolean deleteTrade(StringBuilder sb, String email, String cmnem,
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

    public final synchronized boolean getPosn(StringBuilder sb, String email) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
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
        return true;
    }

    public final synchronized boolean getPosn(StringBuilder sb, String email, String cmnem) {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            return false;
        }
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
        return true;
    }

    public final synchronized boolean getPosn(StringBuilder sb, String email, String cmnem,
            int settlDate) {
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
        posn.print(sb, null);
        return true;
    }
}
