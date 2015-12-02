/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rest;

import static com.swirlycloud.twirly.date.JulianDay.jdToMillis;
import static com.swirlycloud.twirly.date.JulianDay.maybeJdToIso;
import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.io.CacheUtil.NO_CACHE;
import static com.swirlycloud.twirly.util.JsonUtil.PARAMS_NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.Before;

import com.swirlycloud.twirly.app.ServFactory;
import com.swirlycloud.twirly.domain.MarketId;
import com.swirlycloud.twirly.domain.Role;
import com.swirlycloud.twirly.domain.Side;
import com.swirlycloud.twirly.domain.State;
import com.swirlycloud.twirly.entity.Asset;
import com.swirlycloud.twirly.entity.BasicFactory;
import com.swirlycloud.twirly.entity.Contr;
import com.swirlycloud.twirly.entity.Exec;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Market;
import com.swirlycloud.twirly.entity.MarketView;
import com.swirlycloud.twirly.entity.Order;
import com.swirlycloud.twirly.entity.Posn;
import com.swirlycloud.twirly.entity.Trader;
import com.swirlycloud.twirly.exception.BadRequestException;
import com.swirlycloud.twirly.exception.NotFoundException;
import com.swirlycloud.twirly.exception.ServiceUnavailableException;
import com.swirlycloud.twirly.function.UnaryCallback;
import com.swirlycloud.twirly.io.Datastore;
import com.swirlycloud.twirly.mock.MockAsset;
import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockDatastore;
import com.swirlycloud.twirly.mock.MockTrader;
import com.swirlycloud.twirly.node.JslNode;
import com.swirlycloud.twirly.rest.BackUnrest.TransStruct;

public abstract class RestTest {

    private static final @NonNull Factory FACTORY = new BasicFactory();

    protected static final int TODAY = ymdToJd(2014, 2, 11);
    protected static final int SETTL_DAY = TODAY + 2;
    protected static final int EXPIRY_DAY = TODAY + 1;

    protected static final long NOW = jdToMillis(TODAY);

    protected BackUnrest unrest;

    protected static void assertAsset(Asset expected, Asset actual) {
        assertNotNull(actual);
        assertEquals(expected.getMnem(), actual.getMnem());
        assertEquals(expected.getDisplay(), actual.getDisplay());
        assertEquals(expected.getAssetType(), actual.getAssetType());
    }

    protected static void assertAsset(String mnem, Asset actual) {
        assertNotNull(actual);
        assertAsset(MockAsset.newAsset(mnem, FACTORY), actual);
    }

    protected static void assertAssets(final Map<String, ? super Asset> assets) {
        MockAsset.readAsset(FACTORY, new UnaryCallback<Asset>() {
            @Override
            public final void call(Asset arg) {
                assertAsset(arg, (Asset) assets.get(arg.getMnem()));
            }
        });
    }

    protected static void assertContr(Contr expected, Contr actual) {
        assertNotNull(actual);
        assertEquals(expected.getAsset(), actual.getAsset());
        assertEquals(expected.getCcy(), actual.getCcy());
        assertEquals(expected.getTickNumer(), actual.getTickNumer());
        assertEquals(expected.getTickDenom(), actual.getTickDenom());
        assertEquals(expected.getLotNumer(), actual.getLotNumer());
        assertEquals(expected.getLotDenom(), actual.getLotDenom());
        assertEquals(expected.getPipDp(), actual.getPipDp());
        assertEquals(expected.getMinLots(), actual.getMinLots());
        assertEquals(expected.getMaxLots(), actual.getMaxLots());
    }

    protected static void assertContr(String mnem, Contr actual) {
        assertNotNull(actual);
        assertContr(MockContr.newContr(mnem, FACTORY), actual);
    }

    protected static void assertContrs(final Map<String, ? super Contr> contrs) {
        MockContr.readContr(FACTORY, new UnaryCallback<Contr>() {
            @Override
            public final void call(Contr arg) {
                assertEquals(arg, contrs.get(arg.getMnem()));
            }
        });
    }

    protected static void assertMarket(String mnem, String display, String contr, int settlDay,
            int expiryDay, int state, Market actual) {
        assertNotNull(actual);
        assertEquals(mnem, actual.getMnem());
        assertEquals(display, actual.getDisplay());
        assertEquals(contr, actual.getContr());
        assertEquals(settlDay, actual.getSettlDay());
        assertEquals(expiryDay, actual.getExpiryDay());
        assertEquals(state, actual.getState());
    }

    protected static void assertTrader(Trader expected, Trader actual) {
        assertNotNull(actual);
        assertEquals(expected.getMnem(), actual.getMnem());
        assertEquals(expected.getDisplay(), actual.getDisplay());
        assertEquals(expected.getEmail(), actual.getEmail());
    }

    protected static void assertTrader(String mnem, Trader actual) {
        assertNotNull(actual);
        assertTrader(MockTrader.newTrader(mnem, FACTORY), actual);
    }

    protected static void assertTraders(final Map<String, ? super Trader> traders) {
        MockTrader.readTrader(FACTORY, new UnaryCallback<Trader>() {
            @Override
            public final void call(Trader arg) {
                assertEquals(arg, traders.get(arg.getMnem()));
            }
        });
    }

    protected static void assertOrder(String trader, String market, State state, Side side,
            long lots, long ticks, long resd, long exec, long cost, long lastLots, long lastTicks,
            Order actual) {
        assertNotNull(actual);
        assertEquals(trader, actual.getTrader());
        assertEquals(market, actual.getMarket());
        assertNull(actual.getRef());
        assertEquals(state, actual.getState());
        assertEquals(side, actual.getSide());
        assertEquals(lots, actual.getLots());
        assertEquals(ticks, actual.getTicks());
        assertEquals(resd, actual.getResd());
        assertEquals(exec, actual.getExec());
        assertEquals(cost, actual.getCost());
        assertEquals(lastLots, actual.getLastLots());
        assertEquals(lastTicks, actual.getLastTicks());
        assertEquals(1, actual.getMinLots());
        assertEquals(NOW, actual.getCreated());
        assertEquals(NOW, actual.getModified());
    }

    protected static void assertExec(String trader, String market, State state, Side side,
            long lots, long ticks, long resd, long exec, long cost, long lastLots, long lastTicks,
            String contr, int settlDay, Role role, String cpty, Exec actual) {
        assertNotNull(actual);
        assertEquals(trader, actual.getTrader());
        assertEquals(market, actual.getMarket());
        assertNull(actual.getRef());
        assertEquals(state, actual.getState());
        assertEquals(side, actual.getSide());
        assertEquals(lots, actual.getLots());
        assertEquals(ticks, actual.getTicks());
        assertEquals(resd, actual.getResd());
        assertEquals(exec, actual.getExec());
        assertEquals(cost, actual.getCost());
        assertEquals(lastLots, actual.getLastLots());
        assertEquals(lastTicks, actual.getLastTicks());
        assertEquals(1, actual.getMinLots());
        assertEquals(contr, actual.getContr());
        assertEquals(settlDay, actual.getSettlDay());
        assertEquals(role, actual.getRole());
        assertEquals(cpty, actual.getCpty());
        assertEquals(NOW, actual.getCreated());
    }

    protected static void assertPosn(String trader, String market, String contr, int settlDay,
            long buyLots, long buyCost, long sellLots, long sellCost, Posn actual) {
        assertNotNull(actual);
        assertEquals(trader, actual.getTrader());
        assertEquals(contr, actual.getContr());
        assertEquals(settlDay, actual.getSettlDay());
        assertEquals(buyCost, actual.getBuyCost());
        assertEquals(buyLots, actual.getBuyLots());
        assertEquals(sellCost, actual.getSellCost());
        assertEquals(sellLots, actual.getSellLots());
    }

    protected static void assertView(String market, String contr, int settlDay, int level,
            long bidTicks, long bidResd, int bidCount, long offerTicks, long offerResd,
            int offerCount, long lastTicks, long lastLots, long lastTime, MarketView actual)
                    throws NotFoundException, IOException {
        assertNotNull(actual);
        assertEquals(market, actual.getMarket());
        assertEquals(contr, actual.getContr());
        assertEquals(settlDay, actual.getSettlDay());

        assertEquals(bidTicks, actual.getBidTicks(level));
        assertEquals(bidResd, actual.getBidResd(level));
        assertEquals(bidCount, actual.getBidCount(level));

        assertEquals(offerTicks, actual.getOfferTicks(level));
        assertEquals(offerResd, actual.getOfferResd(level));
        assertEquals(offerCount, actual.getOfferCount(level));

        assertEquals(lastTicks, actual.getLastTicks());
        assertEquals(lastLots, actual.getLastLots());
        assertEquals(lastTime, actual.getLastTime());
    }

    protected static void assertView(String market, String contr, int settlDay, MarketView actual)
            throws NotFoundException, IOException {
        assertView(market, contr, settlDay, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, actual);
    }

    protected final Trader postTrader(@NonNull String mnem, String display, @NonNull String email)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        return unrest.postTrader(mnem, display, email, PARAMS_NONE, NOW);
    }

    protected final Trader putTrader(@NonNull String mnem, String display, String email)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        return unrest.putTrader(mnem, display, PARAMS_NONE, NOW);
    }

    protected final Market postMarket(@NonNull String mnem, String display, @NonNull String contr,
            int state) throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        return unrest.postMarket(mnem, display, contr, 0, 0, state, PARAMS_NONE, NOW);
    }

    protected final Market postMarket(@NonNull String mnem, String display, @NonNull String contr,
            int settlDay, int expiryDay, int state) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        return unrest.postMarket(mnem, display, contr, maybeJdToIso(settlDay),
                maybeJdToIso(expiryDay), state, PARAMS_NONE, NOW);
    }

    protected final Market putMarket(@NonNull String trader, @NonNull String mnem, String display,
            int state) throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        return unrest.putMarket(mnem, display, state, PARAMS_NONE, NOW);
    }

    protected final void deleteOrder(@NonNull String trader, @NonNull String market, long id)
            throws BadRequestException, NotFoundException, ServiceUnavailableException,
            IOException {
        unrest.deleteOrder(trader, market, id, NOW);
    }

    protected final void deleteOrder(@NonNull String trader, @NonNull String market,
            @NonNull JslNode first) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        unrest.deleteOrder(trader, market, first, NOW);
    }

    protected final TransStruct postOrder(@NonNull String trader, @NonNull String market,
            long quoteId, @NonNull Side side, long lots, long ticks) throws BadRequestException,
                    NotFoundException, ServiceUnavailableException, IOException {
        return unrest.postOrder(trader, market, null, quoteId, side, lots, ticks, 1, PARAMS_NONE,
                NOW);
    }

    protected final TransStruct putOrder(@NonNull String trader, @NonNull String market, long id,
            long lots) throws BadRequestException, NotFoundException, ServiceUnavailableException,
                    IOException {
        return unrest.putOrder(trader, market, id, lots, PARAMS_NONE, NOW);
    }

    protected final TransStruct putOrder(@NonNull String trader, @NonNull String market,
            @NonNull JslNode first, long lots) throws BadRequestException, NotFoundException,
                    ServiceUnavailableException, IOException {
        return unrest.putOrder(trader, market, first, lots, PARAMS_NONE, NOW);
    }

    protected final void deleteTrade(@NonNull String mnem, @NonNull String market, long id)
            throws BadRequestException, NotFoundException, ServiceUnavailableException {
        unrest.deleteTrade(mnem, market, id, NOW);
    }

    protected final void deleteTrade(@NonNull String mnem, @NonNull String market,
            @NonNull JslNode first)
                    throws BadRequestException, NotFoundException, ServiceUnavailableException {
        unrest.deleteTrade(mnem, market, first, NOW);
    }

    protected final @NonNull JslNode jslList(@NonNull String market, Long... ids) {
        MarketId firstMid = null;
        for (final long id : ids) {
            final MarketId mid = new MarketId(market, id);
            mid.setJslNext(firstMid);
            firstMid = mid;
        }
        assert firstMid != null;
        return firstMid;
    }

    @SuppressWarnings("resource")
    @Before
    public final void setUp() throws BadRequestException, NotFoundException,
            ServiceUnavailableException, InterruptedException, IOException {
        final Datastore datastore = new MockDatastore();
        final BackUnrest unrest = new BackUnrest(datastore, NO_CACHE, new ServFactory(), NOW);
        this.unrest = unrest;
        boolean success = false;
        try {
            postMarket("EURUSD.MAR14", "EURUSD March 14", "EURUSD", SETTL_DAY, EXPIRY_DAY, 0x1);
            postMarket("USDJPY.MAR14", "USDJPY March 14", "USDJPY", SETTL_DAY, EXPIRY_DAY, 0x1);
            success = true;
        } finally {
            if (!success) {
                // Assumption: MockDatastore need not be closed because it does not acquire
                // resources.
                this.unrest = null;
            }
        }
    }

    @After
    public final void tearDown() throws Exception {
        // Assumption: MockDatastore need not be closed because it does not acquire resources.
        unrest = null;
    }
}
