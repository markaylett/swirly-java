/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import static com.swirlycloud.twirly.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.swirlycloud.twirly.entity.BasicFactory;
import com.swirlycloud.twirly.entity.Contr;
import com.swirlycloud.twirly.entity.Factory;
import com.swirlycloud.twirly.entity.Market;
import com.swirlycloud.twirly.mock.MockContr;

public final class MarketTest {

    private static final Factory FACTORY = new BasicFactory();

    @Test
    public final void testToJson() throws IOException {
        final String mnem = "EURUSD.MAR14";
        final String display = "EURUSD March 14";
        final Contr contr = MockContr.newContr("EURUSD", FACTORY);
        final int settlDay = ymdToJd(2014, 2, 14);
        final int expiryDay = ymdToJd(2014, 2, 12);
        final int state = 0x01;
        final Market market = FACTORY.newMarket(mnem, display, contr, settlDay, expiryDay, state);

        final StringBuilder sb = new StringBuilder();

        market.toJson(null, sb);
        assertEquals(
                "{\"mnem\":\"EURUSD.MAR14\",\"display\":\"EURUSD March 14\",\"contr\":\"EURUSD\",\"settlDate\":20140314,\"expiryDate\":20140312,\"state\":1}",
                sb.toString());
    }
}
