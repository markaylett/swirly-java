/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.function;

public interface BinaryPredicate<T, U> {
    boolean call(T lhs, U rhs);
}
