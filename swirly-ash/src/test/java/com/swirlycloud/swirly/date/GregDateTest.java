/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

public final class GregDateTest {

    private static final GregDate DATE_20140314 = new GregDate(2014, 2, 14);

    @Test
    public final void testAccessor() {
        assertTrue(DATE_20140314.getYear() == 2014);
        assertTrue(DATE_20140314.getMon() == 2);
        assertTrue(DATE_20140314.getMDay() == 14);
    }

    @Test
    public final void testEquals() {
        assertTrue(DATE_20140314.equals(new GregDate(2014, 2, 14)));
        assertFalse(DATE_20140314.equals(new GregDate(2015, 2, 14)));
        assertFalse(DATE_20140314.equals(new GregDate(2014, 3, 14)));
        assertFalse(DATE_20140314.equals(new GregDate(2014, 2, 15)));
    }

    @Test
    public final void testCompare() {
        assertTrue(DATE_20140314.compareTo(new GregDate(2014, 2, 14)) == 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2015, 2, 14)) < 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2014, 3, 14)) < 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2014, 2, 15)) < 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2013, 2, 14)) > 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2014, 1, 14)) > 0);
        assertTrue(DATE_20140314.compareTo(new GregDate(2014, 2, 13)) > 0);
    }

    @Test
    public final void testIso() {
        assertEquals(20140314, DATE_20140314.toIso());
        assertEquals(DATE_20140314, GregDate.valueOfIso(20140314));
    }

    @Test
    public final void testJd() {
        assertEquals(2456731, DATE_20140314.toJd());
        assertEquals(DATE_20140314, GregDate.valueOfJd(2456731));
    }

    @Test
    public final void testLeapYear() {
        // Divisible by 100.
        assertFalse(GregDate.isLeapYear(1900));
        assertFalse(new GregDate(1900, 1, 1).isLeapYear());
        // Divisible by 400.
        assertTrue(GregDate.isLeapYear(2000));
        assertTrue(new GregDate(2000, 1, 1).isLeapYear());
        // Divisible by 100.
        assertFalse(GregDate.isLeapYear(2100));
        assertFalse(new GregDate(2100, 1, 1).isLeapYear());
        // Divisible by 4.
        assertTrue(GregDate.isLeapYear(2012));
        assertTrue(new GregDate(2012, 1, 1).isLeapYear());
        // Not divisible by 4.
        assertFalse(GregDate.isLeapYear(2014));
        assertFalse(new GregDate(2014, 1, 1).isLeapYear());
    }

    @Test
    public final void testMDays() {
        // Divisible by 100.
        assertEquals(28, GregDate.mdays(1900, 1));
        assertEquals(28, new GregDate(1900, 1, 1).mdays());
        // Divisible by 400.
        assertEquals(29, GregDate.mdays(2000, 1));
        assertEquals(29, new GregDate(2000, 1, 1).mdays());
        // Divisible by 100.
        assertEquals(28, GregDate.mdays(2100, 1));
        assertEquals(28, new GregDate(2100, 1, 1).mdays());
        // Divisible by 4.
        assertEquals(29, GregDate.mdays(2012, 1));
        assertEquals(29, new GregDate(2012, 1, 1).mdays());
        // Not divisible by 4.
        assertEquals(28, GregDate.mdays(2014, 1));
        assertEquals(28, new GregDate(2014, 1, 1).mdays());
    }

    @Test
    public final void testString() {
        assertEquals("20140314", DATE_20140314.toString());
        assertEquals(DATE_20140314, GregDate.valueOf("20140314"));
    }

    @Test
    public final void testCalendar() {
        final Calendar c = DATE_20140314.toCalendar();
        assertEquals(2014, c.get(Calendar.YEAR));
        assertEquals(2, c.get(Calendar.MONTH));
        assertEquals(14, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        assertEquals(0, c.get(Calendar.SECOND));
        assertEquals(0, c.get(Calendar.MILLISECOND));
    }

    @Test
    public final void testWeekDay() {
        assertEquals(WeekDay.THU, new GregDate(2014, 2, 13).toWeekDay());
        assertEquals(WeekDay.FRI, new GregDate(2014, 2, 14).toWeekDay());
        assertEquals(WeekDay.SAT, new GregDate(2014, 2, 15).toWeekDay());
    }
}
