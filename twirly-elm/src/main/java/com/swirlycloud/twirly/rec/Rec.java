/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.rec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.twirly.node.RbNode;
import com.swirlycloud.twirly.util.Jsonifiable;
import com.swirlycloud.twirly.util.Memorable;

public @NonNullByDefault interface Rec extends Jsonifiable, RbNode, Memorable {

    void setDisplay(@Nullable String display);

    RecType getRecType();

    @Override
    String getMnem();

    String getDisplay();
}
