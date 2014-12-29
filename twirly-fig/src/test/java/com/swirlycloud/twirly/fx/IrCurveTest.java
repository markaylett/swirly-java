/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.fx;

import static com.swirlycloud.twirly.mock.MockIrCurve.newIrCurve;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.swirlycloud.twirly.date.GregDate;

public final class IrCurveTest {
    private static final double DELTA = 0.000000000001;
    @Test
    public final void test() throws IOException, ParserConfigurationException, SAXException {

        final IrCurve irc = newIrCurve("GBP");

        assertEquals(new GregDate(2014, 11, 25), irc.getEffectiveAsOf());
        assertEquals("GBP", irc.getCcy());
        assertEquals(DayCount.ACTUAL365FIXED, irc.getDayCount());
        assertEquals(new GregDate(2014, 11, 25), irc.getSpotDate());

        // 1M.
        assertEquals(0.005016, irc.getRate(new GregDate(2015, 0, 26)), DELTA);
        // 2M.
        assertEquals(0.005240, irc.getRate(new GregDate(2015, 1, 25)), DELTA);
        // 3M.
        assertEquals(0.005596, irc.getRate(new GregDate(2015, 2, 25)), DELTA);
        // 6M.
        assertEquals(0.006823, irc.getRate(new GregDate(2015, 5, 25)), DELTA);
        // 12M.
        assertEquals(0.009774, irc.getRate(new GregDate(2015, 11, 25)), DELTA);

        // Interpolated.
        assertEquals(0.0062095, irc.getRate(new GregDate(2015, 4, 10)), DELTA);
    }
}
