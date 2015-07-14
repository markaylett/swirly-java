/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.web;

import static com.swirlycloud.twirly.date.JulianDay.maybeIsoToJd;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.app.LockableServ;
import com.swirlycloud.twirly.app.Sess;
import com.swirlycloud.twirly.app.Trans;
import com.swirlycloud.twirly.domain.Action;
import com.swirlycloud.twirly.domain.Contr;
import com.swirlycloud.twirly.domain.Exec;
import com.swirlycloud.twirly.domain.Market;
import com.swirlycloud.twirly.domain.RecType;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Trader;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.io.AsyncDatastore;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.util.Params;

public final @NonNullByDefault class BackRest extends RestImpl implements Rest {

    public BackRest(LockableServ serv) {
        super(serv);
    }

    public BackRest(AsyncDatastore datastore, long now) throws InterruptedException,
            ExecutionException {
        this(new LockableServ(datastore, now));
    }

    public BackRest(Datastore datastore, long now) {
        this(new LockableServ(datastore, now));
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
    public final void getView(String marketMnem, Params params, long now, Appendable out)
            throws NotFoundException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireRead();
        try {
            doGetView(marketMnem, params, now, out);
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
            final Sess sess = serv.findSessByEmail(email);
            if (sess == null) {
                throw new NotFoundException(String.format("trader '%s' has no orders", email));
            }
            serv.archiveOrder(sess, market, id, now);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void postOrder(String email, String marketMnem, @Nullable String ref,
            Action action, long ticks, long lots, long minLots, Params params, long now,
            Appendable out) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Sess sess = serv.getLazySessByEmail(email);
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new NotFoundException(String.format("market '%s' does not exist", marketMnem));
            }
            try (final Trans trans = new Trans()) {
                serv.placeOrder(sess, market, ref, action, ticks, lots, minLots, now, trans);
                trans.toJson(params, out);
            }
        } finally {
            serv.releaseWrite();
        }
    }

    public final void putOrder(String email, String marketMnem, long id, long lots, Params params,
            long now, Appendable out) throws BadRequestException, NotFoundException,
            ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Sess sess = serv.findSessByEmail(email);
            if (sess == null) {
                throw new NotFoundException(String.format("trader '%s' has no orders", email));
            }
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new NotFoundException(String.format("market '%s' does not exist", marketMnem));
            }
            try (final Trans trans = new Trans()) {
                if (lots > 0) {
                    serv.reviseOrder(sess, market, id, lots, now, trans);
                } else {
                    serv.cancelOrder(sess, market, id, now, trans);
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
            final Sess sess = serv.findSessByEmail(email);
            if (sess == null) {
                throw new NotFoundException(String.format("trader '%s' has no trades", email));
            }
            serv.archiveTrade(sess, market, id, now);
        } finally {
            serv.releaseWrite();
        }
    }

    public final void postTrade(String trader, String marketMnem, String ref, Action action,
            long ticks, long lots, Role role, String cpty, Params params, long now, Appendable out)
            throws NotFoundException, ServiceUnavailableException, IOException {
        final LockableServ serv = (LockableServ) this.serv;
        serv.acquireWrite();
        try {
            final Sess sess = serv.getLazySess(trader);
            final Market market = (Market) serv.findRec(RecType.MARKET, marketMnem);
            if (market == null) {
                throw new NotFoundException(String.format("market '%s' does not exist", marketMnem));
            }
            final Exec trade = serv.createTrade(sess, market, ref, action, ticks, lots, role, cpty,
                    now);
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
