/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.app;

import java.util.Calendar;
import java.util.TimeZone;

import org.eclipse.jdt.annotation.NonNull;

import com.swirlycloud.twirly.date.GregDate;

public final class DateUtil {

    private DateUtil() {
    }

    public static final TimeZone NYC = TimeZone.getTimeZone("America/New_York");

    /**
     * Get the business date from a transaction time.
     * 
     * Business dates roll at 5pm New York.
     * 
     * @param ms
     *            The milliseconds since epoch.
     * @return the business date.
     */
    @NonNull
    public static GregDate getBusDate(long ms) {
        final Calendar cal = Calendar.getInstance(NYC);
        cal.setTimeInMillis(ms);
        // Add 7 hours to 17.00 will effectively roll the date.
        cal.add(Calendar.HOUR_OF_DAY, 7);
        // Zero time components now that we're on the correct date.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return GregDate.valueOf(cal);
    }
}
