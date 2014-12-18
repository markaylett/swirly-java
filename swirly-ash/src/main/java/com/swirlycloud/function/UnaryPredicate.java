/*******************************************************************************
 * Copyright (C) 2013, 2014 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.function;

public interface UnaryPredicate<T> {
    boolean call(T arg);
}
