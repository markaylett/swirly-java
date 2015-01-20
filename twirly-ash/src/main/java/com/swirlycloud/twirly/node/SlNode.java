/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.node;

public interface SlNode {

    void setSlNext(SlNode next);

    SlNode slNext();
}
