/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.node;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Singly-linked node.
 * 
 * @author Mark Aylett
 */
public interface SlNode {

    void setSlNext(@Nullable SlNode next);

    @Nullable
    SlNode slNext();
}
