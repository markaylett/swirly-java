/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.date;

import static com.swirlycloud.twirly.date.DateUtil.getBusDate;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class DateUtilTest {

    @Test
    public final void testBusDate() {
        // Business dates roll at 5pm New York.

        // Friday, March 14, 2014
        // 21.00 UTC
        // 17.00 EDT (UTC-4 hours)

        // 20.59 UTC
        assertEquals(new GregDate(2014, 2, 14), getBusDate(1394830799000L));
        // 21.00 UTC
        assertEquals(new GregDate(2014, 2, 15), getBusDate(1394830800000L));
    }
}