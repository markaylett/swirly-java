/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.util;

import java.io.IOException;

import com.swirlycloud.function.UnaryFunction;

public interface Jsonifiable {
    void toJson(UnaryFunction<String, String> params, Appendable out) throws IOException;
}
