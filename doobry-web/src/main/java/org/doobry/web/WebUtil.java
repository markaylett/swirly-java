/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package org.doobry.web;

import java.io.PrintWriter;

public final class WebUtil {
    private WebUtil() {
    }

    public static void writeError(PrintWriter pw, String msg) {
        pw.write("{\"msg\":\"");
        pw.write(msg);
        pw.write("\"}");
    }
}
