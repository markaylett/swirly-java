/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.date;

import static com.swirlycloud.date.JulianDay.ymdToJd;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class WeekDayTest {
    @Test
    public final void test() {
        assertEquals(WeekDay.THU, WeekDay.valueOfJd(ymdToJd(2014, 2, 13)));
        assertEquals(WeekDay.FRI, WeekDay.valueOfJd(ymdToJd(2014, 2, 14)));
        assertEquals(WeekDay.SAT, WeekDay.valueOfJd(ymdToJd(2014, 2, 15)));
    }
}
