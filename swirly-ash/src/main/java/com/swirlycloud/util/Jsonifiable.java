/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;

import com.swirlycloud.function.UnaryFunction;

public interface Jsonifiable {
    void toJson(UnaryFunction<String, String> params, Appendable out) throws IOException;
}
