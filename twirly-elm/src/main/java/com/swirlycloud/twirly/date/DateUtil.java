/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.date;

import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public final class DateUtil {

    private DateUtil() {
    }

    public static final DateTimeZone NY = DateTimeZone.forID("America/New_York");

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
        // Add 7 hours to 17.00 will effectively roll the date.
        return GregDate.valueOf(new DateTime(ms, NY).plusHours(7));
    }
}
