/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.back;

import java.io.PrintWriter;

public final class WebUtil {
    private WebUtil() {
    }

    public static void writeError(PrintWriter pw, int num, String msg) {
        pw.write("{\"num\":");
        pw.write(num);
        pw.write(",\"msg\":\"");
        pw.write(msg);
        pw.write("\"}");
    }

    // FIXME: this is a temporary hack.
    public static String alternateEmail(String email) {
        if (!email.endsWith("@googlemail.com")) {
            return null;
        }
        final String name = email.substring(0, email.length() - 15);
        return name + "@gmail.com";
    }
}
