/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;

public interface Jsonifiable {
    void toJson(Appendable out, Object arg) throws IOException;
}
