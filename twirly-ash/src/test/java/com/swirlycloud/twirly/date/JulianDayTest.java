/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.date;

import static com.swirlycloud.twirly.date.JulianDay.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class JulianDayTest {
    @Test
    public final void testYmdToIso() {
        assertEquals(20140314, ymdToIso(2014, 2, 14));
    }

    @Test
    public final void testYmdToJd() {
        assertEquals(2456731, ymdToJd(2014, 2, 14));
        // AD 1978 January 1, 0h UT is JD 2443509.5 and AD 1978 July 21, 15h UT, is JD 2443711.125.
        assertEquals(2443510, ymdToJd(1978, 0, 1));
        assertEquals(2443711, ymdToJd(1978, 6, 21));
    }

    @Test
    public final void testJdToMillis() {
        assertEquals(1394798400000L, jdToMillis(ymdToJd(2014, 2, 14)));
    }
}
