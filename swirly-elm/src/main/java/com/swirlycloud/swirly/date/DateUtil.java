/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.date;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class DateUtil {

    private DateUtil() {
    }

    public static final DateTimeZone NY = DateTimeZone.forID("America/New_York");

    /**
     * Get the business day from a transaction time.
     * 
     * Business day rolls at 5pm New York.
     * 
     * @param ms
     *            Milliseconds since epoch.
     * @return the business day.
     */
    @NonNull
    public static GregDate getBusDay(long ms) {
        // Add 7 hours to 17.00 will effectively roll the date.
        return GregDate.valueOf(new DateTime(ms, NY).plusHours(7));
    }
}
