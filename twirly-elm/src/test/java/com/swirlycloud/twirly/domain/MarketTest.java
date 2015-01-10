/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.mock.MockTrader;
import com.swirlycloud.twirly.util.Params;

public final class MarketTest {
    @Test
    public final void testToJson() throws IOException {
        final Contr contr = MockContr.newContr("EURUSD");
        final int settlDay = ymdToJd(2014, 2, 14);
        final Market market = new Market(contr, settlDay, settlDay, settlDay);

        final Trader trader = MockTrader.newTrader("MARAYL");
        final long now = 1414932078620L;

        market.placeOrder(new Order(1, trader, contr, settlDay, "apple", Action.BUY, 12343, 10, 0,
                now), now);
        market.placeOrder(new Order(2, trader, contr, settlDay, "orange", Action.BUY, 12344, 5, 0,
                now), now);
        market.placeOrder(new Order(3, trader, contr, settlDay, "pear", Action.SELL, 12346, 5, 0,
                now), now);
        market.placeOrder(new Order(4, trader, contr, settlDay, "banana", Action.SELL, 12346, 2, 0,
                now), now);

        final StringBuilder sb = new StringBuilder();

        // Null params.
        market.toJson(null, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344,12343,null],\"bidLots\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerLots\":[7,null,null],\"offerCount\":[2,null,null],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());

        // Empty params.
        sb.setLength(0);
        market.toJson(new Params() {
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344,12343,null],\"bidLots\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerLots\":[7,null,null],\"offerCount\":[2,null,null],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());

        // Explicit TOB.
        sb.setLength(0);
        market.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(1) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344],\"bidLots\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[7],\"offerCount\":[2],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());

        // Round-up to minimum.
        sb.setLength(0);
        market.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(-1) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344],\"bidLots\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[7],\"offerCount\":[2],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());

        // Between minimum and maximum.
        sb.setLength(0);
        market.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(2) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344,12343],\"bidLots\":[5,10],\"bidCount\":[1,1],\"offerTicks\":[12346,null],\"offerLots\":[7,null],\"offerCount\":[2,null],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());

        // Round-down to maximum.
        sb.setLength(0);
        market.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(100) : null;
            }
        }, sb);
        assertEquals(
                "{\"id\":803163,\"contr\":\"EURUSD\",\"settlDate\":20140314,\"fixingDate\":20140314,\"expiryDate\":20140314,\"bidTicks\":[12344,12343,null,null,null],\"bidLots\":[5,10,null,null,null],\"bidCount\":[1,1,null,null,null],\"offerTicks\":[12346,null,null,null,null],\"offerLots\":[7,null,null,null,null],\"offerCount\":[2,null,null,null,null],\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null}",
                sb.toString());
    }
}
