/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.rest;

import static com.swirlycloud.swirly.date.DateUtil.getBusDate;
import static com.swirlycloud.swirly.date.JulianDay.maybeIsoToJd;
import static com.swirlycloud.swirly.rest.RestUtil.getExpiredParam;
import static com.swirlycloud.swirly.util.JsonUtil.toJsonArray;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.app.LockableServ;
import com.swirlycloud.swirly.app.Response;
import com.swirlycloud.swirly.app.Serv;
import com.swirlycloud.swirly.book.MarketBook;
import com.swirlycloud.swirly.domain.RecType;
import com.swirlycloud.swirly.domain.Role;
import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.EntitySet;
import com.swirlycloud.swirly.entity.Exec;
import com.swirlycloud.swirly.entity.Market;
import com.swirlycloud.swirly.entity.Order;
import com.swirlycloud.swirly.entity.Posn;
import com.swirlycloud.swirly.entity.Quote;
import com.swirlycloud.swirly.entity.Rec;
import com.swirlycloud.swirly.entity.Trader;
import com.swirlycloud.swirly.entity.TraderSess;
import com.swirlycloud.swirly.exception.BadRequestException;
import com.swirlycloud.swirly.exception.LiquidityUnavailableException;
import com.swirlycloud.swirly.exception.MarketClosedException;
import com.swirlycloud.swirly.exception.NotFoundException;
import com.swirlycloud.swirly.exception.OrderNotFoundException;
import com.swirlycloud.swirly.exception.ServiceUnavailableException;
import com.swirlycloud.swirly.io.Cache;
import com.swirlycloud.swirly.io.Datastore;
import com.swirlycloud.swirly.io.Journ;
import com.swirlycloud.swirly.io.Model;
import com.swirlycloud.swirly.node.JslNode;
import com.swirlycloud.swirly.node.RbNode;
import com.swirlycloud.swirly.util.Params;

public final @NonNullByDefault class BackRest implements Rest {

    private final Serv serv;
    private final Response resp = new Response();
    private volatile long timeout;

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

    public BackRest(Model model, Journ journ, Cache cache, long now)
            throws NotFoundException, ServiceUnavailableException, InterruptedException {
        this(new LockableServ(model, journ, cache, now));
    }

    public BackRest(Datastore datastore, Cache cache, long now)
            throws NotFoundException, ServiceUnavailableException, InterruptedException {
        this(new LockableServ(datastore, cache, now));
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
    public final void getRec(EntitySet es, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            int i = 0;
            out.append('{');
            if (es.isAssetSet()) {
                out.append("\"assets\":");
                toJsonArray(serv.getFirstRec(RecType.ASSET), params, out);
                ++i;
            }
            if (es.isContrSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"contrs\":");
                toJsonArray(serv.getFirstRec(RecType.CONTR), params, out);
                ++i;
            }
            if (es.isMarketSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"markets\":");
                toJsonArray(serv.getFirstRec(RecType.MARKET), params, out);
                ++i;
            }
            if (es.isTraderSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"traders\":");
                toJsonArray(serv.getFirstRec(RecType.TRADER), params, out);
            }
            out.append('}');
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            toJsonArray(serv.getFirstRec(recType), params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final Rec rec = serv.findRec(recType, mnem);
            if (rec == null) {
                throw new NotFoundException(String.format("record '%s' does not exist", mnem));
            }
            rec.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getSess(String trader, EntitySet es, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            int i = 0;
            out.append('{');
            if (es.isOrderSet()) {
                out.append("\"orders\":");
                toJsonArray(sess.getFirstOrder(), params, out);
                ++i;
            }
            if (es.isTradeSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"trades\":");
                toJsonArray(sess.getFirstTrade(), params, out);
                ++i;
            }
            if (es.isPosnSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"posns\":");
                toJsonArray(sess.getFirstPosn(), params, out);
                ++i;
            }
            if (es.isQuoteSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"quotes\":");
                toJsonArray(sess.getFirstQuote(), params, out);
                ++i;
            }
            if (es.isViewSet()) {
                if (i > 0) {
                    out.append(',');
                }
                out.append("\"views\":");
                getView(serv.getFirstRec(RecType.MARKET), params, now, out);
            }
            out.append('}');
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstOrder(), params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.filterMarket(sess.getFirstOrder(), market, params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getOrder(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            final Order order = sess.findOrder(market, id);
            if (order == null) {
                throw new OrderNotFoundException(String.format("order '%d' does not exist", id));
            }
            order.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstTrade(), params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.filterMarket(sess.getFirstTrade(), market, params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getTrade(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            final Exec trade = sess.findTrade(market, id);
            if (trade == null) {
                throw new NotFoundException(String.format("trade '%d' does not exist", id));
            }
            trade.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstPosn(), params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.filterPosn(sess.getFirstPosn(), contr, params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getPosn(String trader, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            final Posn posn = sess.findPosn(contr, maybeIsoToJd(settlDate));
            if (posn == null) {
                throw new NotFoundException(
                        String.format("posn for '%s' on '%d' does not exist", contr, settlDate));
            }
            posn.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getQuote(String trader, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            toJsonArray(sess.getFirstQuote(), params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getQuote(String trader, String market, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            RestUtil.filterMarket(sess.getFirstQuote(), market, params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getQuote(String trader, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final TraderSess sess = serv.getTrader(trader);
            final Quote quote = sess.findQuote(market, id);
            if (quote == null) {
                throw new NotFoundException(String.format("quote '%d' does not exist", id));
            }
            quote.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getView(Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            getView(serv.getFirstRec(RecType.MARKET), params, now, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        int lock = serv.writeLock();
        try {
            serv.poll(now);
            lock = serv.demoteLock();
            final MarketBook book = serv.getMarket(market);
            final boolean withExpired = getExpiredParam(params);
            final int busDay = getBusDate(now).toJd();
            if (!withExpired && book.isExpiryDaySet() && book.getExpiryDay() < busDay) {
                throw new MarketClosedException(
                        String.format("market '%s' has expired", book.getMnem()));
            }
            book.toJsonView(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    @Override
    public final long getTimeout() {
        return timeout;
    }

    public final void postTrader(String mnem, @Nullable String display, String email, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final Trader trader = serv.createTrader(mnem, display, email);
            trader.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void putTrader(String mnem, @Nullable String display, Params params, long now,
            Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final Trader trader = serv.updateTrader(mnem, display);
            trader.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void postMarket(String mnem, @Nullable String display, String contr, int settlDate,
            int expiryDate, int state, Params params, long now, Appendable out)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final int settlDay = maybeIsoToJd(settlDate);
            final int expiryDay = maybeIsoToJd(expiryDate);
            final Market market = serv.createMarket(mnem, display, contr, settlDay, expiryDay,
                    state, now);
            market.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void putMarket(String mnem, @Nullable String display, int state, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final Market market = serv.updateMarket(mnem, display, state, now);
            market.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void deleteOrder(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveOrder(sess, market, id, now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void deleteOrder(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveOrder(sess, market, first, now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void postOrder(String trader, String market, @Nullable String ref, long quoteId,
            Side side, long lots, long ticks, long minLots, Params params, long now, Appendable out)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            serv.createOrder(sess, book, ref, quoteId, side, lots, ticks, minLots, now, resp);
            resp.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            resp.clearAll();
            serv.unlock(lock);
        }
    }

    public final void putOrder(String trader, String market, long id, long lots, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            if (lots > 0) {
                serv.reviseOrder(sess, book, id, lots, now, resp);
            } else {
                serv.cancelOrder(sess, book, id, now, resp);
            }
            resp.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            resp.clearAll();
            serv.unlock(lock);
        }
    }

    public final void putOrder(String trader, String market, JslNode first, long lots,
            Params params, long now, Appendable out) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            if (lots > 0) {
                serv.reviseOrder(sess, book, first, lots, now, resp);
            } else {
                serv.cancelOrder(sess, book, first, now, resp);
            }
            resp.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            resp.clearAll();
            serv.unlock(lock);
        }
    }

    public final void postQuote(String trader, String market, @Nullable String ref, Side side,
            long lots, Params params, long now, Appendable out)
                    throws LiquidityUnavailableException, NotFoundException,
                    ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            final Quote quote = serv.createQuote(sess, book, ref, side, lots, now);
            quote.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void deleteTrade(String trader, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveTrade(sess, market, id, now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void deleteTrade(String trader, String market, JslNode first, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            serv.archiveTrade(sess, market, first, now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void postTrade(String trader, String market, String ref, Side side, long lots,
            long ticks, Role role, String cpty, Params params, long now, Appendable out)
                    throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            final Exec trade = serv.createTrade(sess, book, ref, side, lots, ticks, role, cpty,
                    now);
            trade.toJson(params, out);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    // Tasks.

    public final void endOfDay(long now) throws NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
            serv.expireEndOfDay(now);
            serv.settlEndOfDay(now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }

    public final void poll(long now) throws NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        final int lock = serv.writeLock();
        try {
            serv.poll(now);
        } finally {
            timeout = serv.getTimeout();
            serv.unlock(lock);
        }
    }
}
