/*******************************************************************************
 * Copyright (C) 2013, 2014 Mark Aylett <mark.aylett@gmail.com>
 *
 * All rights reserved.
 *******************************************************************************/
package com.swirlycloud.collection;

public interface SlNode {

    void setSlNext(SlNode next);

    SlNode slNext();
}