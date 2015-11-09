/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static com.swirlycloud.twirly.util.JsonUtil.parseStartObject;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.stream.JsonParser;

import org.junit.Test;

import com.swirlycloud.twirly.util.Params;

public final class MarketViewTest {
    @Test
    public final void testToJson() throws IOException {
        final String mnem = "EURUSD.MAR14";
        final String contr = "EURUSD";
        final int settlDay = ymdToJd(2014, 2, 14);
        final MarketData data = new MarketData();

        data.setOfferLevel(2, 12348, 30, 15, 3);
        data.setOfferLevel(1, 12347, 20, 10, 2);
        data.setOfferLevel(0, 12346, 10, 5, 1);

        data.setBidLevel(0, 12344, 10, 5, 1);
        data.setBidLevel(1, 12343, 20, 10, 2);
        data.setBidLevel(2, 12342, 30, 15, 3);

        final long lastLots = 5;
        final long lastTicks = 12344;
        final long lastTime = 1414932078620L;

        final MarketView view = new MarketView(mnem, contr, settlDay, lastLots, lastTicks, lastTime,
                data);

        final StringBuilder sb = new StringBuilder();

        // Null params.
        view.toJson(null, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344,12343,12342],\"bidResd\":[10,20,30],\"bidQuot\":[5,10,15],\"bidCount\":[1,2,3],\"offerTicks\":[12346,12347,12348],\"offerResd\":[10,20,30],\"offerQuot\":[5,10,15],\"offerCount\":[1,2,3]}",
                sb.toString());

        // Empty params.
        sb.setLength(0);
        view.toJson(new Params() {
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344,12343,12342],\"bidResd\":[10,20,30],\"bidQuot\":[5,10,15],\"bidCount\":[1,2,3],\"offerTicks\":[12346,12347,12348],\"offerResd\":[10,20,30],\"offerQuot\":[5,10,15],\"offerCount\":[1,2,3]}",
                sb.toString());

        // Explicit TOB.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(1) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344],\"bidResd\":[10],\"bidQuot\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerResd\":[10],\"offerQuot\":[5],\"offerCount\":[1]}",
                sb.toString());

        // Round-up to minimum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(-1) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344],\"bidResd\":[10],\"bidQuot\":[5],\"bidCount\":[1],\"offerTicks\":[12346],\"offerResd\":[10],\"offerQuot\":[5],\"offerCount\":[1]}",
                sb.toString());

        // Between minimum and maximum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(2) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344,12343],\"bidResd\":[10,20],\"bidQuot\":[5,10],\"bidCount\":[1,2],\"offerTicks\":[12346,12347],\"offerResd\":[10,20],\"offerQuot\":[5,10],\"offerCount\":[1,2]}",
                sb.toString());

        // Round-down to maximum.
        sb.setLength(0);
        view.toJson(new Params() {
            @SuppressWarnings("unchecked")
            @Override
            public final <T> T getParam(String name, Class<T> clazz) {
                return "depth".equals(name) ? (T) Integer.valueOf(100) : null;
            }
        }, sb);
        assertEquals(
                "{\"market\":\"EURUSD.MAR14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"lastLots\":5,\"lastTicks\":12344,\"lastTime\":1414932078620,\"bidTicks\":[12344,12343,12342,null,null],\"bidResd\":[10,20,30,null,null],\"bidQuot\":[5,10,15,null,null],\"bidCount\":[1,2,3,null,null],\"offerTicks\":[12346,12347,12348,null,null],\"offerResd\":[10,20,30,null,null],\"offerQuot\":[5,10,15,null,null],\"offerCount\":[1,2,3,null,null]}",
                sb.toString());
    }

    @Test
    public final void testParse() throws IOException {
        final String mnem = "EURUSD.MAR14";
        final String contr = "EURUSD";
        final int settlDay = ymdToJd(2014, 2, 14);
        final MarketData data = new MarketData();

        data.setOfferLevel(2, 12348, 30, 15, 3);
        data.setOfferLevel(1, 12347, 20, 10, 2);
        data.setOfferLevel(0, 12346, 10, 5, 1);

        data.setBidLevel(0, 12344, 10, 5, 1);
        data.setBidLevel(1, 12343, 20, 10, 2);
        data.setBidLevel(2, 12342, 30, 15, 3);

        final long lastLots = 5;
        final long lastTicks = 12344;
        final long lastTime = 1414932078620L;

        final MarketView in = new MarketView(mnem, contr, settlDay, lastLots, lastTicks, lastTime,
                data);
        System.out.println(in.toString());
        try (final JsonParser p = Json.createParser(new StringReader(in.toString()))) {
            assert p != null;
            parseStartObject(p);
            final MarketView out = MarketView.parse(p);

            assertEquals(mnem, out.getMarket());

            assertEquals(0, out.getOfferTicks(3));
            assertEquals(0, out.getOfferResd(3));
            assertEquals(0, out.getOfferCount(3));

            assertEquals(12348, out.getOfferTicks(2));
            assertEquals(30, out.getOfferResd(2));
            assertEquals(3, out.getOfferCount(2));

            assertEquals(12347, out.getOfferTicks(1));
            assertEquals(20, out.getOfferResd(1));
            assertEquals(2, out.getOfferCount(1));

            assertEquals(12346, out.getOfferTicks(0));
            assertEquals(10, out.getOfferResd(0));
            assertEquals(1, out.getOfferCount(0));

            assertEquals(12344, out.getBidTicks(0));
            assertEquals(10, out.getBidResd(0));
            assertEquals(1, out.getOfferCount(0));

            assertEquals(12343, out.getBidTicks(1));
            assertEquals(20, out.getBidResd(1));
            assertEquals(2, out.getOfferCount(1));

            assertEquals(12342, out.getBidTicks(2));
            assertEquals(30, out.getBidResd(2));
            assertEquals(3, out.getOfferCount(2));

            assertEquals(0, out.getBidTicks(3));
            assertEquals(0, out.getBidResd(3));
            assertEquals(0, out.getOfferCount(3));

            assertEquals(lastLots, out.getLastLots());
            assertEquals(lastTicks, out.getLastTicks());
            assertEquals(lastTime, out.getLastTime());
        }
    }
}
