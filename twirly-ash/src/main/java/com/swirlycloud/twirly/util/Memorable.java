/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.util;

import java.io.Serializable;

import org.eclipse.jdt.annotation.NonNullByDefault;

public @NonNullByDefault interface Memorable extends Serializable {
    String getMnem();
}
