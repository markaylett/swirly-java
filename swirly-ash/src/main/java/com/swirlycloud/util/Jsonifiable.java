/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;
import java.util.Map;

public interface Jsonifiable {
    void toJson(Map<String, String> params, Appendable out) throws IOException;
}
