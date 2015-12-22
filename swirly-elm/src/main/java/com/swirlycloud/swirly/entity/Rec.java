/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.swirly.entity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.swirlycloud.swirly.domain.RecType;

public @NonNullByDefault interface Rec extends Entity {

    void setDisplay(@Nullable String display);

    RecType getRecType();

    String getMnem();

    String getDisplay();
}
