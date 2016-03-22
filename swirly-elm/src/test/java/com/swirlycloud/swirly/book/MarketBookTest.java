/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.book;

import static com.swirlycloud.swirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.swirlycloud.swirly.domain.Side;
import com.swirlycloud.swirly.entity.Factory;
import com.swirlycloud.swirly.util.Params;

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
        final String contr = "EURUSD";
        final int settlDay = ymdToJd(2014, 2, 14);
        final int expiryDay = ymdToJd(2014, 2, 12);
        final int state = 0x01;
        final MarketBook book = (MarketBook) factory.newMarket(mnem, display, contr, settlDay,
                expiryDay, state);

        final long now = 1414932078620L;

        book.createOrder(factory.newOrder("MARAYL", book.getMarket(), book.getContr(),
                book.getSettlDay(), 1, "apple", 0, Side.BUY, 10, 12343, 0, now), now);
        book.createOrder(factory.newOrder("MARAYL", book.getMarket(), book.getContr(),
                book.getSettlDay(), 2, "orange", 0, Side.BUY, 5, 12344, 0, now), now);
        book.createOrder(factory.newOrder("MARAYL", book.getMarket(), book.getContr(),
                book.getSettlDay(), 3, "pear", 0, Side.SELL, 5, 12346, 0, now), now);
        book.createOrder(factory.newOrder("MARAYL", book.getMarket(), book.getContr(),
                book.getSettlDay(), 4, "banana", 0, Side.SELL, 2, 12346, 0, now), now);

        final StringBuilder sb = new StringBuilder();

        // Null params.
        book.toJsonView(null, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null],\"bidResd\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerResd\":[7,null,null],\"offerCount\":[2,null,null]}",
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
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null],\"bidResd\":[5,10,null],\"bidCount\":[1,1,null],\"offerTicks\":[12346,null,null],\"offerResd\":[7,null,null],\"offerCount\":[2,null,null]}",
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
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344],\"bidResd\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerResd\":[7],\"offerCount\":[2]}",
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
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344],\"bidResd\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerResd\":[7],\"offerCount\":[2]}",
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
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344,12343],\"bidResd\":[5,10],\"bidCount\":[1,1],\"offerTicks\":[12346,null],\"offerResd\":[7,null],\"offerCount\":[2,null]}",
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
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":null,\"lastTicks\":null,\"lastTime\":null,\"bidTicks\":[12344,12343,null,null,null],\"bidResd\":[5,10,null,null,null],\"bidCount\":[1,1,null,null,null],\"offerTicks\":[12346,null,null,null,null],\"offerResd\":[7,null,null,null,null],\"offerCount\":[2,null,null,null,null]}",
                sb.toString());
    }
}
