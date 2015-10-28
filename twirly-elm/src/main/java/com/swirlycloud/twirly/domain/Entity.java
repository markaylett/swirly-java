/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.domain;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Jsonifiable;

public @NonNullByDefault interface Entity extends Jsonifiable, RbNode {
}
