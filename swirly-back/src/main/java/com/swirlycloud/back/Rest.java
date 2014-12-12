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
import com.swirlycloud.domain.Trader;
import com.swirlycloud.engine.Accnt;
import com.swirlycloud.engine.Model;
import com.swirlycloud.engine.Serv;
import com.swirlycloud.engine.Trans;
import com.swirlycloud.exception.BadRequestException;
import com.swirlycloud.exception.ForbiddenException;
import com.swirlycloud.exception.NotFoundException;
import com.swirlycloud.util.RbNode;
import com.swirlycloud.util.SlNode;

public final class Rest {

    private final Serv serv;

    private final void doGetRec(RecType recType, Appendable out) throws IOException {
        out.append('[');
        SlNode node = serv.getFirstRec(recType);
        for (int i = 0; node != null; node = node.slNext()) {
            final Rec rec = (Rec) node;
            if (i > 0) {
                out.append(',');
            }
            rec.toJson(out);
            ++i;
        }
        out.append(']');
    }

    private final void doGetOrder(Accnt accnt, String email, Appendable out) throws IOException {
        out.append('[');
        RbNode node = accnt.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (i > 0) {
                out.append(',');
            }
            order.toJson(out);
            ++i;
        }
        out.append(']');
    }

    private final void doGetTrade(Accnt accnt, String email, Appendable out) throws IOException {
        out.append('[');
        RbNode node = accnt.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(out);
            ++i;
        }
        out.append(']');
    }

    private final void doGetPosn(Accnt accnt, String email, Appendable out) throws IOException {
        out.append('[');
        RbNode node = accnt.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (i > 0) {
                out.append(',');
            }
            posn.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public Rest(Model model) {
        serv = new Serv(model);
    }

    public final synchronized void getRec(boolean isAdmin, Appendable out) throws IOException {
        out.append("{\"assets\":");
        doGetRec(RecType.ASSET, out);
        out.append(",\"contrs\":");
        doGetRec(RecType.CONTR, out);
        if (isAdmin) {
            out.append(",\"traders\":");
            doGetRec(RecType.TRADER, out);
        }
        out.append('}');
    }

    public final synchronized void getRec(RecType recType, Appendable out) throws IOException {
        doGetRec(recType, out);
    }

    public final synchronized void getRec(RecType recType, String mnem, Appendable out)
            throws NotFoundException, IOException {
        final Rec rec = serv.findRec(recType, mnem);
        if (rec == null) {
            throw new NotFoundException(String.format("record '%s' does not exist", mnem));
        }
        rec.toJson(out);
    }

    public final synchronized void postTrader(String mnem, String display, String email,
            Appendable out) throws BadRequestException, IOException {
        final Trader trader = serv.createTrader(mnem, display, email);
        trader.toJson(out);
    }

    public final synchronized void getMarket(Appendable out) throws IOException {
        out.append('[');
        RbNode node = serv.getFirstMarket();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (i > 0) {
                out.append(',');
            }
            market.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getMarket(String cmnem, Appendable out)
            throws NotFoundException, IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
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
            market.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getMarket(String cmnem, int settlDate, Appendable out)
            throws NotFoundException, IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Market market = serv.findMarket(contr, settlDay);
        if (market == null) {
            throw new NotFoundException(String.format("market for '%s' on '%d' does not exist",
                    cmnem, settlDate));
        }
        market.toJson(out);
    }

    public final synchronized void postMarket(String cmnem, int settlDate, int expiryDate,
            Appendable out) throws BadRequestException, NotFoundException, IOException {
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final int expiryDay = isoToJd(expiryDate);
        final Market market = serv.createMarket(contr, settlDay, expiryDay);
        market.toJson(out);
    }

    public final synchronized void getAccnt(String email, Appendable out) throws NotFoundException,
            IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("{\"orders\":[],\"trades\":[],\"posns\":[]}");
            return;
        }
        out.append("{\"orders\":");
        doGetOrder(accnt, email, out);
        out.append(",\"trades\":");
        doGetTrade(accnt, email, out);
        out.append(",\"posns\":");
        doGetPosn(accnt, email, out);
        out.append('}');
    }

    public final synchronized void deleteOrder(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException, IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no orders", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        serv.archiveOrder(accnt, contr.getId(), settlDay, id);
    }

    public final synchronized void getOrder(String email, Appendable out) throws NotFoundException,
            IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt != null) {
            out.append("[]");
            return;
        }
        doGetOrder(accnt, email, out);
    }

    public final synchronized void getOrder(String email, String cmnem, Appendable out)
            throws ForbiddenException, NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
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
            order.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getOrder(String email, String cmnem, int settlDate,
            Appendable out) throws ForbiddenException, NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
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
            order.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getOrder(String email, String cmnem, int settlDate, long id,
            Appendable out) throws IOException, NotFoundException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no orders", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Order order = accnt.findOrder(contr.getId(), settlDay, id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        order.toJson(out);
    }

    public final synchronized void postOrder(String email, String cmnem, int settlDate, String ref,
            Action action, long ticks, long lots, long minLots, Appendable out)
            throws BadRequestException, NotFoundException, IOException {
        final Accnt accnt = serv.getLazyAccntByEmail(email);
        final Market market = serv.getLazyMarket(cmnem, isoToJd(settlDate));
        if (market == null) {
            throw new NotFoundException(String.format("market for '%s' on '%d' does not exist",
                    cmnem, settlDate));
        }
        final Trans trans = serv.placeOrder(accnt, market, ref, action, ticks, lots, minLots,
                new Trans());
        trans.toJson(out);
    }

    public final synchronized void putOrder(String email, String cmnem, int settlDate, long id,
            long lots, Appendable out) throws BadRequestException, NotFoundException, IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no orders", email));
        }
        final Market market = serv.findMarket(cmnem, isoToJd(settlDate));
        if (market == null) {
            throw new NotFoundException(String.format("market for '%s' on '%d' does not exist",
                    cmnem, settlDate));
        }
        final Trans trans = new Trans();
        if (lots > 0) {
            serv.reviseOrder(accnt, market, id, lots, trans);
        } else {
            serv.cancelOrder(accnt, market, id, trans);
        }
        trans.toJson(out);
    }

    public final synchronized void deleteTrade(String email, String cmnem, int settlDate, long id)
            throws BadRequestException, NotFoundException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no trades", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        serv.archiveTrade(accnt, contr.getId(), settlDay, id);
    }

    public final synchronized void getTrade(String email, Appendable out) throws NotFoundException,
            IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
        }
        doGetTrade(accnt, email, out);
    }

    public final synchronized void getTrade(String email, String cmnem, Appendable out)
            throws ForbiddenException, NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
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
            trade.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getTrade(String email, String cmnem, int settlDate,
            Appendable out) throws ForbiddenException, NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
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
            trade.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getTrade(String email, String cmnem, int settlDate, long id,
            Appendable out) throws NotFoundException, IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no trades", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Exec trade = accnt.findTrade(contr.getId(), settlDay, id);
        if (trade == null) {
            throw new NotFoundException(String.format("trade '%d' does not exist", id));
        }
        trade.toJson(out);
    }

    public final synchronized void getPosn(String email, Appendable out) throws NotFoundException,
            IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
        }
        doGetPosn(accnt, email, out);
    }

    public final synchronized void getPosn(String email, String cmnem, Appendable out)
            throws ForbiddenException, NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        if (serv.findRec(RecType.CONTR, cmnem) == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final Accnt accnt = serv.findAccnt(trader);
        if (accnt == null) {
            out.append("[]");
            return;
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
            posn.toJson(out);
            ++i;
        }
        out.append(']');
    }

    public final synchronized void getPosn(String email, String cmnem, int settlDate, Appendable out)
            throws NotFoundException, IOException {
        final Accnt accnt = serv.findAccntByEmail(email);
        if (accnt == null) {
            throw new NotFoundException(String.format("trader '%s' has no posns", email));
        }
        final Contr contr = (Contr) serv.findRec(RecType.CONTR, cmnem);
        if (contr == null) {
            throw new NotFoundException(String.format("contract '%s' does not exist", cmnem));
        }
        final int settlDay = isoToJd(settlDate);
        final Posn posn = accnt.findPosn(contr, settlDay);
        if (posn == null) {
            throw new NotFoundException(String.format("posn for '%s' on '%d' does not exist",
                    cmnem, settlDate));
        }
        posn.toJson(out);
    }
}
