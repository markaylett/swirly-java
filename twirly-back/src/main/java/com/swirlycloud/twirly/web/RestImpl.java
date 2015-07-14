/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.app.Sess;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Params;

public abstract @NonNullByDefault class RestImpl {

    protected final Serv serv;

    private final boolean getExpiredParam(Params params) {
        final Boolean val = params.getParam("expired", Boolean.class);
        return val == null ? false : val.booleanValue();
    }

    private final void doGetRec(RecType recType, Params params, Appendable out) throws IOException {
        toJsonArray(serv.getFirstRec(recType), params, out);
    }

    private final void doGetOrder(Sess sess, Params params, Appendable out) throws IOException {
        toJsonArray(sess.getFirstOrder(), params, out);
    }

    private final void doGetTrade(Sess sess, Params params, Appendable out) throws IOException {
        toJsonArray(sess.getFirstTrade(), params, out);
    }

    private final void doGetPosn(Sess sess, Params params, Appendable out) throws IOException {
        toJsonArray(sess.getFirstPosn(), params, out);
    }

    private final void doGetPosn(Sess sess, String contr, Params params, Appendable out)
            throws IOException {
        out.append('[');
        RbNode node = sess.getFirstPosn();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Posn posn = (Posn) node;
            if (!posn.getContr().equals(contr)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            posn.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    protected final void doGetRec(boolean withTraders, Params params, long now, Appendable out)
            throws IOException {
        out.append("{\"assets\":");
        doGetRec(RecType.ASSET, params, out);
        out.append(",\"contrs\":");
        doGetRec(RecType.CONTR, params, out);
        out.append(",\"markets\":");
        doGetRec(RecType.MARKET, params, out);
        if (withTraders) {
            out.append(",\"traders\":");
            doGetRec(RecType.TRADER, params, out);
        }
        out.append('}');
    }

    protected final void doGetRec(RecType recType, Params params, long now, Appendable out)
            throws IOException {
        doGetRec(recType, params, out);
    }

    protected final void doGetRec(RecType recType, String mnem, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Rec rec = serv.findRec(recType, mnem);
        if (rec == null) {
            throw new NotFoundException(String.format("record '%s' does not exist", mnem));
        }
        rec.toJson(params, out);
    }

    protected final void doGetView(Params params, long now, Appendable out) throws IOException {
        final boolean withExpired = getExpiredParam(params);
        final int busDay = getBusDate(now).toJd();
        out.append('[');
        RbNode node = serv.getFirstRec(RecType.MARKET);
        for (int i = 0; node != null; node = node.rbNext()) {
            final Market market = (Market) node;
            if (!withExpired && market.isExpiryDaySet() && market.getExpiryDay() < busDay) {
                // Ignore expired contracts.
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            market.toJsonView(params, out);
            ++i;
        }
        out.append(']');
    }

    protected final void doGetView(String marketMnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final boolean withExpired = getExpiredParam(params);
        final int busDay = getBusDate(now).toJd();
        final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
        if (market == null) {
            throw new NotFoundException(String.format("market '%s' does not exist", marketMnem));
        }
        if (!withExpired && market.isExpiryDaySet() && market.getExpiryDay() < busDay) {
            throw new NotFoundException(String.format("market '%s' has expired", marketMnem));
        }
        market.toJsonView(params, out);
    }

    protected final void doGetSess(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("{\"orders\":[],\"trades\":[],\"posns\":[]}");
            return;
        }
        out.append("{\"orders\":");
        doGetOrder(sess, params, out);
        out.append(",\"trades\":");
        doGetTrade(sess, params, out);
        out.append(",\"posns\":");
        doGetPosn(sess, params, out);
        out.append('}');
    }

    protected final void doGetOrder(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        doGetOrder(sess, params, out);
    }

    protected final void doGetOrder(String email, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        out.append('[');
        RbNode node = sess.getFirstOrder();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Order order = (Order) node;
            if (!order.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            order.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    protected final void doGetOrder(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Sess sess = serv.findSessByEmail(email);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' has no orders", email));
        }
        final Order order = sess.findOrder(market, id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        order.toJson(params, out);
    }

    protected final void doGetTrade(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        doGetTrade(sess, params, out);
    }

    protected final void doGetTrade(String email, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        out.append('[');
        RbNode node = sess.getFirstTrade();
        for (int i = 0; node != null; node = node.rbNext()) {
            final Exec trade = (Exec) node;
            if (!trade.getMarket().equals(market)) {
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            trade.toJson(params, out);
            ++i;
        }
        out.append(']');
    }

    protected final void doGetTrade(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Sess sess = serv.findSessByEmail(email);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' has no trades", email));
        }
        final Exec trade = sess.findTrade(market, id);
        if (trade == null) {
            throw new NotFoundException(String.format("trade '%d' does not exist", id));
        }
        trade.toJson(params, out);
    }

    protected final void doGetPosn(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        doGetPosn(sess, params, out);
    }

    protected final void doGetPosn(String email, String contr, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final Trader trader = serv.findTraderByEmail(email);
        if (trader == null) {
            throw new NotFoundException(String.format("trader '%s' does not exist", email));
        }
        final Sess sess = serv.findSess(trader.getMnem());
        if (sess == null) {
            out.append("[]");
            return;
        }
        doGetPosn(sess, contr, params, out);
    }

    protected final void doGetPosn(String email, String contr, int settlDate, Params params,
            long now, Appendable out) throws NotFoundException, IOException {
        final Sess sess = serv.findSessByEmail(email);
        if (sess == null) {
            throw new NotFoundException(String.format("trader '%s' has no posns", email));
        }
        final Posn posn = sess.findPosn(contr, maybeIsoToJd(settlDate));
        if (posn == null) {
            throw new NotFoundException(String.format("posn for '%s' on '%d' does not exist",
                    contr, settlDate));
        }
        posn.toJson(params, out);
    }

    protected RestImpl(Serv serv) {
        this.serv = serv;
    }
}
