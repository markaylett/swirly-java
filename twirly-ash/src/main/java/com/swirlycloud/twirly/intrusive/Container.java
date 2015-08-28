/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.intrusive;

import org.eclipse.jdt.annotation.NonNull;

public interface Container<V> {

    void clear();

    void add(@NonNull V node);

    V getFirst();

    boolean isEmpty();
}
