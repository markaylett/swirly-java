/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.MarketBook;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Rec;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Serv;
import com.swirlycloud.twirly.domain.TraderSess;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Params;

abstract @NonNullByDefault class RestImpl {

    protected final Serv serv;

    private final boolean getExpiredParam(Params params) {
        final Boolean val = params.getParam("expired", Boolean.class);
        return val == null ? false : val.booleanValue();
    }

    private final void doGetRec(RecType recType, Params params, Appendable out) throws IOException {
        toJsonArray(serv.getFirstRec(recType), params, out);
    }

    private final void doGetOrder(TraderSess sess, Params params, Appendable out)
            throws IOException {
        toJsonArray(sess.getFirstOrder(), params, out);
    }

    private final void doGetTrade(TraderSess sess, Params params, Appendable out)
            throws IOException {
        toJsonArray(sess.getFirstTrade(), params, out);
    }

    private final void doGetPosn(TraderSess sess, Params params, Appendable out) throws IOException {
        toJsonArray(sess.getFirstPosn(), params, out);
    }

    private final void doGetPosn(TraderSess sess, String contr, Params params, Appendable out)
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
            final MarketBook book = (MarketBook) node;
            if (!withExpired && book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
                // Ignore expired contracts.
                continue;
            }
            if (i > 0) {
                out.append(',');
            }
            book.toJsonView(params, out);
            ++i;
        }
        out.append(']');
    }

    protected final void doGetView(String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final boolean withExpired = getExpiredParam(params);
        final int busDay = getBusDate(now).toJd();
        final MarketBook book = serv.getMarket(market);
        if (!withExpired && book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
            throw new NotFoundException(String.format("market '%s' has expired", market));
        }
        book.toJsonView(params, out);
    }

    protected final void doGetSess(String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        out.append("{\"orders\":");
        doGetOrder(sess, params, out);
        out.append(",\"trades\":");
        doGetTrade(sess, params, out);
        out.append(",\"posns\":");
        doGetPosn(sess, params, out);
        out.append('}');
    }

    protected final void doGetOrder(String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        doGetOrder(sess, params, out);
    }

    protected final void doGetOrder(String mnem, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
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

    protected final void doGetOrder(String mnem, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        final Order order = sess.findOrder(market, id);
        if (order == null) {
            throw new NotFoundException(String.format("order '%d' does not exist", id));
        }
        order.toJson(params, out);
    }

    protected final void doGetTrade(String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        doGetTrade(sess, params, out);
    }

    protected final void doGetTrade(String mnem, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
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

    protected final void doGetTrade(String mnem, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        final Exec trade = sess.findTrade(market, id);
        if (trade == null) {
            throw new NotFoundException(String.format("trade '%d' does not exist", id));
        }
        trade.toJson(params, out);
    }

    protected final void doGetPosn(String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        doGetPosn(sess, params, out);
    }

    protected final void doGetPosn(String mnem, String contr, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
        doGetPosn(sess, contr, params, out);
    }

    protected final void doGetPosn(String mnem, String contr, int settlDate, Params params,
            long now, Appendable out) throws NotFoundException, IOException {
        final TraderSess sess = serv.getTrader(mnem);
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
