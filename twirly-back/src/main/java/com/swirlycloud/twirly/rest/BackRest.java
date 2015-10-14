/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.twirly.rest.RestUtil.getExpiredParam;
import static com.swirlycloud.twirly.rest.RestUtil.getViewsParam;
import static com.swirlycloud.twirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.app.LockableServ;
import com.swirlycloud.twirly.app.Serv;
import com.swirlycloud.twirly.app.Trans;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.MarketBook;
import com.swirlycloud.twirly.domain.Order;
import com.swirlycloud.twirly.domain.Posn;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.TraderSess;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.MarketClosedException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.OrderNotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Cache;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.rec.Contr;
import com.swirlycloud.twirly.rec.Market;
import com.swirlycloud.twirly.rec.Rec;
import com.swirlycloud.twirly.rec.RecType;
import com.swirlycloud.twirly.rec.Trader;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class BackRest implements Rest {

    private final Serv serv;
    private final Trans trans = new Trans();

    private static void getView(@Nullable RbNode first, Params params, long now, Appendable out)
            throws IOException {
        final boolean withExpired = getExpiredParam(params);
        final int busDay = getBusDate(now).toJd();
        out.append('[');
        int i = 0;
        for (RbNode node = first; node != null; node = node.rbNext()) {
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

    public BackRest(Model model, Journ journ, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(new LockableServ(model, journ, cache, factory, now));
    }

    public BackRest(Datastore datastore, Cache cache, Factory factory, long now)
            throws InterruptedException {
        this(new LockableServ(datastore, cache, factory, now));
    }

    public BackRest(LockableServ serv) {
        this.serv = serv;
    }

    @Override
    public final @Nullable String findTraderByEmail(String email) {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final Trader trader = serv.findTraderByEmail(email);
            return trader != null ? trader.getMnem() : null;
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            out.append("{\"assets\":");
            toJsonArray(serv.getFirstRec(RecType.ASSET), params, out);
            out.append(",\"contrs\":");
            toJsonArray(serv.getFirstRec(RecType.CONTR), params, out);
            out.append(",\"markets\":");
            toJsonArray(serv.getFirstRec(RecType.MARKET), params, out);
            if (withTraders) {
                out.append(",\"traders\":");
                toJsonArray(serv.getFirstRec(RecType.TRADER), params, out);
            }
            out.append('}');
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            toJsonArray(serv.getFirstRec(recType), params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final Rec rec = serv.findRec(recType, mnem);
            if (rec == null) {
                throw new NotFoundException(String.format("record '%s' does not exist", mnem));
            }
            rec.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getView(Params params, long now, Appendable out) throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            getView(serv.getFirstRec(RecType.MARKET), params, now, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final MarketBook book = serv.getMarket(market);
            final boolean withExpired = getExpiredParam(params);
            final int busDay = getBusDate(now).toJd();
            if (!withExpired && book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
                throw new MarketClosedException(
                        String.format("market '%s' has expired", book.getMnem()));
            }
            book.toJsonView(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getSess(String trader, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            out.append("{\"orders\":");
            toJsonArray(sess.getFirstOrder(), params, out);
            out.append(",\"trades\":");
            toJsonArray(sess.getFirstTrade(), params, out);
            out.append(",\"posns\":");
            toJsonArray(sess.getFirstPosn(), params, out);
            if (getViewsParam(params)) {
                out.append(",\"views\":");
                getView(serv.getFirstRec(RecType.MARKET), params, now, out);
            }
            out.append('}');
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstOrder(), params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.getOrder(sess.getFirstOrder(), market, params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final Order order = sess.findOrder(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            order.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstTrade(), params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.getTrade(sess.getFirstTrade(), market, params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final Exec trade = sess.findTrade(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
            trade.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstPosn(), params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.getPosn(sess.getFirstPosn(), contr, params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.readLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final Posn posn = sess.findPosn(contr, maybeIsoToJd(settlDate));
            if (posn == null) {
                throw new NotFoundException(
                        String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
            }
            posn.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void postTrader(String mnem, @Nullable String display, String email, Params params,
            long now, Appendable out)
                    throws BadRequestException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final Trader trader = serv.createTrader(mnem, display, email);
            trader.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void putTrader(String mnem, @Nullable String display, Params params, long now,
            Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final Trader trader = serv.updateTrader(mnem, display);
            trader.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void postMarket(String mnem, @Nullable String display, String contrMnem,
            int settlDate, int expiryDate, int state, Params params, long now, Appendable out)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final Contr contr = (Contr) serv.findRec(RecType.CONTR, contrMnem);
            if (contr == null) {
                throw new NotFoundException(
                        String.format("contract '%s' does not exist", contrMnem));
            }
            final int settlDay = maybeIsoToJd(settlDate);
            final int expiryDay = maybeIsoToJd(expiryDate);
            final Market market = serv.createMarket(mnem, display, contr, settlDay, expiryDay,
                    state, now);
            market.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void putMarket(String mnem, @Nullable String display, int state, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final Market market = serv.updateMarket(mnem, display, state, now);
            market.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void deleteOrder(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveOrder(sess, market, id, now);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void deleteOrder(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveOrder(sess, market, first, now);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void postOrder(String trader, String market, @Nullable String ref, Side side,
            long ticks, long lots, long minLots, Params params, long now, Appendable out)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            serv.placeOrder(sess, book, ref, side, ticks, lots, minLots, now, trans);
            trans.toJson(params, out);
        } finally {
            trans.clear();
            serv.unlock(lock);
        }
    }

    public final void putOrder(String trader, String market, long id, long lots, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            if (lots > 0) {
                serv.reviseOrder(sess, book, id, lots, now, trans);
            } else {
                serv.cancelOrder(sess, book, id, now, trans);
            }
            trans.toJson(params, out);
        } finally {
            trans.clear();
            serv.unlock(lock);
        }
    }

    public final void putOrder(String trader, String market, JslNode first, long lots,
            Params params, long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            if (lots > 0) {
                serv.reviseOrder(sess, book, first, lots, now, trans);
            } else {
                serv.cancelOrder(sess, book, first, now, trans);
            }
            trans.toJson(params, out);
        } finally {
            trans.clear();
            serv.unlock(lock);
        }
    }

    public final void deleteTrade(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveTrade(sess, market, id, now);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void deleteTrade(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveTrade(sess, market, first, now);
        } finally {
            serv.unlock(lock);
        }
    }

    public final void postTrade(String trader, String market, String ref, Side side, long ticks,
            long lots, Role role, String cpty, Params params, long now, Appendable out)
                    throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            final Exec trade = serv.createTrade(sess, book, ref, side, ticks, lots, role, cpty,
                    now);
            trade.toJson(params, out);
        } finally {
            serv.unlock(lock);
        }
    }

    // Cron jobs.

    public final void getEndOfDay(long now) throws NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.expireEndOfDay(now);
            serv.settlEndOfDay(now);
        } finally {
            serv.unlock(lock);
        }
    }
}
