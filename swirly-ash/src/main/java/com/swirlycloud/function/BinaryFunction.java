/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.function;

public interface BinaryFunction<R, T, U> {
    R call(T lhs, U rhs);
}
