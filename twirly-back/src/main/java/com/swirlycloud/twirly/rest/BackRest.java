/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.domain.Factory;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.LockableServ;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.MarketBook;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.domain.TraderSess;
import com.swirlycloud.twirly.domain.Trans;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.io.Journ;
import com.swirlycloud.twirly.io.Model;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class BackRest extends RestImpl implements Rest {

    public BackRest(LockableServ serv) {
        super(serv);
    }

    public BackRest(Model model, Journ journ, Factory factory, long now)
            throws InterruptedException {
        this(new LockableServ(model, journ, factory, now));
    }

    public BackRest(Datastore datastore, Factory factory, long now) throws InterruptedException {
        this(new LockableServ(datastore, factory, now));
    }

    @Override
    public final void getRec(boolean withTraders, Params params, long now, Appendable out)
            throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetRec(withTraders, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getRec(RecType recType, Params params, long now, Appendable out)
            throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetRec(recType, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getRec(RecType recType, String mnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetRec(recType, mnem, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getView(Params params, long now, Appendable out) throws IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetView(params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getView(String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetView(market, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getSess(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetSess(email, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getOrder(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetOrder(email, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getOrder(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetOrder(email, market, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getOrder(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetOrder(email, market, id, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getTrade(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetTrade(email, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getTrade(String email, String market, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetTrade(email, market, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getTrade(String email, String market, long id, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetTrade(email, market, id, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getPosn(String email, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetPosn(email, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getPosn(String email, String contr, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetPosn(email, contr, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    @Override
    public final void getPosn(String email, String contr, int settlDate, Params params, long now,
            Appendable out) throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetPosn(email, contr, settlDate, params, now, out);
        } finally {
            serv.releaseRead();
        }
    }

    public final void postTrader(String mnem, String display, String email, Params params,
            long now, Appendable out) throws BadRequestException, ServiceUnavailableException,
            IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Trader trader = serv.createTrader(mnem, display, email);
            trader.toJson(params, out);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void putTrader(String mnem, String display, Params params, long now, Appendable out)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Trader trader = serv.updateTrader(mnem, display);
            trader.toJson(params, out);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void postMarket(String mnem, String display, String contrMnem, int settlDate,
            int expiryDate, int state, Params params, long now, Appendable out)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
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
            serv.releaseWrite();
        }
    }

    public final void putMarket(String mnem, String display, int state, Params params, long now,
            Appendable out) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Market market = serv.updateMarket(mnem, display, state, now);
            market.toJson(params, out);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void deleteOrder(String email, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final TraderSess sess = serv.getTraderByEmail(email);
            serv.archiveOrder(sess, market, id, now);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void postOrder(String email, String market, @Nullable String ref, Side side,
            long ticks, long lots, long minLots, Params params, long now, Appendable out)
            throws BadRequestException, NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final TraderSess sess = serv.getTraderByEmail(email);
            final MarketBook book = serv.getMarket(market);
            try (final Trans trans = new Trans()) {
                serv.placeOrder(sess, book, ref, side, ticks, lots, minLots, now, trans);
                trans.toJson(params, out);
            }
        } finally {
            serv.releaseWrite();
        }
    }

    public final void putOrder(String email, String market, long id, long lots, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final TraderSess sess = serv.getTraderByEmail(email);
            final MarketBook book = serv.getMarket(market);
            try (final Trans trans = new Trans()) {
                if (lots > 0) {
                    serv.reviseOrder(sess, book, id, lots, now, trans);
                } else {
                    serv.cancelOrder(sess, book, id, now, trans);
                }
                trans.toJson(params, out);
            }
        } finally {
            serv.releaseWrite();
        }
    }

    public final void deleteTrade(String email, String market, long id, long now)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final TraderSess sess = serv.getTraderByEmail(email);
            serv.archiveTrade(sess, market, id, now);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void postTrade(String trader, String market, String ref, Side side, long ticks,
            long lots, Role role, String cpty, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final TraderSess sess = serv.getTrader(trader);
            final MarketBook book = serv.getMarket(market);
            final Exec trade = serv
                    .createTrade(sess, book, ref, side, ticks, lots, role, cpty, now);
            trade.toJson(params, out);
        } finally {
            serv.releaseWrite();
        }
    }

    // Cron jobs.

    public final void getEndOfDay(long now) throws NotFoundException, ServiceUnavailableException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            serv.expireMarkets(now);
            serv.settlMarkets(now);
        } finally {
            serv.releaseWrite();
        }
    }
}
