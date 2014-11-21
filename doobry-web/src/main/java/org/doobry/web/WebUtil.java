/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.io.PrintWriter;
import java.util.regex.Pattern;

public final class WebUtil {
    private static final String[] EMPTY = {};
    private static final Pattern PATTERN = Pattern.compile("/");

    private WebUtil() {
    }

    public static String[] splitPathInfo(String pathInfo) {
        if (pathInfo == null) {
            return EMPTY;
        }
        int begin = 0;
        int end = pathInfo.length();
        if (end > 0) {
            if (pathInfo.charAt(0) == '/') {
                ++begin;
            }
            if (end > 1 && pathInfo.charAt(end - 1) == '/') {
                --end;
            }
        }
        if (end - begin == 0) {
            return EMPTY;
        }
        return PATTERN.split(pathInfo.substring(begin, end));
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
