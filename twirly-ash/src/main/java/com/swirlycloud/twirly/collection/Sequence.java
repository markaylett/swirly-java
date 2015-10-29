/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.collection;

import org.eclipse.jdt.annotation.NonNull;

public interface Sequence<V> {

    void clear();

    void add(@NonNull V node);

    V getFirst();

    boolean isEmpty();
}
