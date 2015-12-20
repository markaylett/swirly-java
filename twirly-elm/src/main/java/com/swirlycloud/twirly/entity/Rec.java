/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

public @NonNullByDefault interface Rec extends Entity {

    void setDisplay(@Nullable String display);

    RecType getRecType();

    String getMnem();

    String getDisplay();
}
