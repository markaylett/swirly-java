/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.function;

public interface BinaryCallback<T, U> {
    void call(T lhs, U rhs);
}
