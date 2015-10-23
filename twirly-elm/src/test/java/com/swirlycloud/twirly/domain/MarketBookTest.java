/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.twirly.mock.MockContr;
import com.swirlycloud.twirly.rec.Contr;
import com.swirlycloud.twirly.util.Params;

public final class MarketBookTest {

    private Factory factory;

    @Before
    public final void setUp() {
        factory = new BookFactory();
    }

    @Test
    public final void testToJsonView() throws IOException {
        final String mnem = "EURUSD.MAR14";
        final String display = "EURUSD March 14";
        final Contr contr = MockContr.newContr("EURUSD", factory);
        final int settlDay = ymdToJd(2014, 2, 14);
        final int expiryDay = ymdToJd(2014, 2, 12);
        final int state = 0x01;
        final MarketBook book = (MarketBook) factory.newMarket(mnem, display, contr, settlDay,
                expiryDay, state);

        final long now = 1414932078620L;

        book.createOrder(factory.newOrder(1, "MARAYL", book, "apple", Side.BUY, 12343, 10, 0, now),
                now);
        book.createOrder(factory.newOrder(2, "MARAYL", book, "orange", Side.BUY, 12344, 5, 0, now),
                now);
        book.createOrder(factory.newOrder(3, "MARAYL", book, "pear", Side.SELL, 12346, 5, 0, now),
                now);
        book.createOrder(factory.newOrder(4, "MARAYL", book, "banana", Side.SELL, 12346, 2, 0, now),
                now);

        final StringBuilder sb = new StringBuilder();

        // Null params.
        book.toJsonView(null, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null],\"bidLots\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerLots\":[7,null,null],\"offerCount\":[2,null,null]}",
                sb.toString());

        // Empty params.
        sb.setLength(0);
        book.toJsonView(new Params() {
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null],\"bidLots\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerLots\":[7,null,null],\"offerCount\":[2,null,null]}",
                sb.toString());

        // Explicit TOB.
        sb.setLength(0);
        book.toJsonView(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(1) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344],\"bidLots\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[7],\"offerCount\":[2]}",
                sb.toString());

        // Round-up to minimum.
        sb.setLength(0);
        book.toJsonView(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(-1) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344],\"bidLots\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerLots\":[7],\"offerCount\":[2]}",
                sb.toString());

        // Between minimum and maximum.
        sb.setLength(0);
        book.toJsonView(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(2) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344,12343],\"bidLots\":[5,10],\"bidCount\":[1,1],\"offerTicks\":[12346,null],\"offerLots\":[7,null],\"offerCount\":[2,null]}",
                sb.toString());

        // Round-down to maximum.
        sb.setLength(0);
        book.toJsonView(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(100) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastTicks\":null,\"lastLots\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null,null,null],\"bidLots\":[5,10,null,null,null],\"bidCount\":[1,1,null,null,null],\"offerTicks\":[12346,null,null,null,null],\"offerLots\":[7,null,null,null,null],\"offerCount\":[2,null,null,null,null]}",
                sb.toString());
    }
}
