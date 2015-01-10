/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.IOException;

public interface Jsonifiable {
    void toJson(Params params, Appendable out) throws IOException;
}
